package com.relfor.pcs.payroll.service.ops;

import com.relfor.pcs.payroll.entity.PlatformProduct;
import com.relfor.pcs.payroll.repository.PlatformProductRepository;
import com.relfor.pcs.payroll.repository.TenantCompanyMappingRepository;
import com.relfor.pcs.payroll.security.Permissions;
import com.relfor.pcs.payroll.util.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class OpsProductService {

    public static final String FALLBACK_DEFAULT_CODE = "devourin";

    private final PlatformProductRepository productRepository;
    private final TenantCompanyMappingRepository tenantRepository;

    public OpsProductService(
            PlatformProductRepository productRepository,
            TenantCompanyMappingRepository tenantRepository) {
        this.productRepository = productRepository;
        this.tenantRepository = tenantRepository;
    }

    public List<Map<String, Object>> listActive() {
        return catalogOrder(productRepository.findAllByActiveTrueOrderBySortOrderAscIdAsc()).stream()
                .map((product) -> toView(product, false))
                .toList();
    }

    public List<Map<String, Object>> listCatalog() {
        if (!SecurityUtils.hasAuthority(Permissions.SUPERADMIN)) {
            throw OpsException.forbidden("Only superadmin can manage the product catalog.");
        }
        return catalogOrder(productRepository.findAllByOrderBySortOrderAscIdAsc()).stream()
                .map((product) -> toView(product, true))
                .toList();
    }

    public List<Map<String, Object>> listForPicker() {
        return listActive();
    }

    public PlatformProduct requireDefault() {
        return productRepository.findFirstByDefaultProductTrue()
                .filter((item) -> Boolean.TRUE.equals(item.getActive()))
                .or(() -> productRepository.findByCodeIgnoreCase(FALLBACK_DEFAULT_CODE))
                .or(() -> productRepository.findAllByActiveTrueOrderBySortOrderAscIdAsc().stream().findFirst())
                .orElseThrow(() -> OpsException.badRequest("Add a source product before onboarding a client."));
    }

    public PlatformProduct resolve(Map<String, Object> body) {
        Long productId = asLong(body == null ? null : body.get("productId"));
        if (productId != null) {
            return productRepository.findById(productId)
                    .filter((item) -> Boolean.TRUE.equals(item.getActive()))
                    .orElseThrow(() -> OpsException.badRequest("Product is not in the catalog."));
        }
        String code = stringVal(body == null ? null : body.get("productCode"));
        if (code != null) {
            return productRepository.findByCodeIgnoreCase(code)
                    .filter((item) -> Boolean.TRUE.equals(item.getActive()))
                    .orElseThrow(() -> OpsException.badRequest("Product is not in the catalog."));
        }
        return requireDefault();
    }

    public PlatformProduct resolveId(Long productId) {
        if (productId != null) {
            return productRepository.findById(productId)
                    .orElseGet(() -> productRepository.findFirstByDefaultProductTrue()
                            .or(() -> productRepository.findByCodeIgnoreCase(FALLBACK_DEFAULT_CODE))
                            .orElse(null));
        }
        return productRepository.findFirstByDefaultProductTrue()
                .or(() -> productRepository.findByCodeIgnoreCase(FALLBACK_DEFAULT_CODE))
                .orElse(null);
    }

    @Transactional
    public Map<String, Object> create(Map<String, Object> body) {
        PlatformProduct product = new PlatformProduct();
        applyFields(product, body, true);
        if (productRepository.findAll().isEmpty()) {
            product.setDefaultProduct(true);
        }
        if (Boolean.TRUE.equals(product.getDefaultProduct())) {
            clearOtherDefaults(null);
            product.setActive(true);
        }
        return toView(productRepository.save(product), true);
    }

    @Transactional
    public Map<String, Object> update(Long id, Map<String, Object> body) {
        PlatformProduct product = productRepository.findById(id)
                .orElseThrow(() -> OpsException.notFound("Product not found."));
        boolean turningOff = body.containsKey("active") && !asBool(body.get("active"), true);
        if (turningOff && Boolean.TRUE.equals(product.getDefaultProduct())) {
            throw OpsException.badRequest("The default source product must stay active.");
        }
        applyFields(product, body, false);
        if (Boolean.TRUE.equals(product.getDefaultProduct())) {
            clearOtherDefaults(product.getId());
            product.setActive(true);
        }
        return toView(productRepository.save(product), true);
    }

    public Map<String, Object> toView(PlatformProduct product) {
        return toView(product, false);
    }

    public Map<String, Object> toView(PlatformProduct product, boolean includeUsage) {
        if (product == null) {
            return null;
        }
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("id", product.getId());
        view.put("code", product.getCode());
        view.put("displayName", product.getDisplayName());
        view.put("description", product.getDescription());
        view.put("category", nameOf(product.getCategory(), PlatformProduct.Category.BILLING));
        view.put("origin", nameOf(product.getOrigin(), PlatformProduct.Origin.THIRD_PARTY));
        view.put("integrationMode", nameOf(product.getIntegrationMode(), PlatformProduct.IntegrationMode.STANDALONE));
        view.put("standalone", product.getIntegrationMode() != PlatformProduct.IntegrationMode.CONNECTOR);
        view.put("defaultProduct", Boolean.TRUE.equals(product.getDefaultProduct()));
        view.put("active", Boolean.TRUE.equals(product.getActive()));
        view.put("sortOrder", product.getSortOrder() == null ? 0 : product.getSortOrder());
        if (includeUsage && product.getId() != null) {
            view.put("clientCount", tenantRepository.countByProductId(product.getId()));
        }
        return view;
    }

    private void applyFields(PlatformProduct product, Map<String, Object> body, boolean creating) {
        String displayName = stringVal(body.get("displayName"));
        if (creating && (displayName == null || displayName.isBlank())) {
            throw OpsException.badRequest("displayName is required.");
        }
        if (displayName != null && !displayName.isBlank()) {
            product.setDisplayName(displayName);
        }
        String code = stringVal(body.get("code"));
        if (creating && (code == null || code.isBlank())) {
            code = slug(product.getDisplayName());
        }
        if (code != null && !code.isBlank()) {
            code = slug(code);
            Long currentId = product.getId() == null ? -1L : product.getId();
            if (productRepository.existsByCodeIgnoreCaseAndIdNot(code, currentId)
                    || (creating && productRepository.findByCodeIgnoreCase(code).isPresent())) {
                throw OpsException.conflict("A product with this code already exists.");
            }
            product.setCode(code);
        }
        if (creating && (product.getCode() == null || product.getCode().isBlank())) {
            throw OpsException.badRequest("code is required.");
        }
        if (body.containsKey("description")) {
            product.setDescription(stringVal(body.get("description")));
        }
        if (creating || body.containsKey("category")) {
            product.setCategory(parseEnum(body.get("category"), PlatformProduct.Category.class, PlatformProduct.Category.BILLING));
        }
        if (creating || body.containsKey("origin")) {
            product.setOrigin(parseEnum(body.get("origin"), PlatformProduct.Origin.class, PlatformProduct.Origin.THIRD_PARTY));
        }
        if (creating || body.containsKey("integrationMode")) {
            product.setIntegrationMode(parseEnum(
                    body.get("integrationMode"),
                    PlatformProduct.IntegrationMode.class,
                    PlatformProduct.IntegrationMode.STANDALONE));
        }
        if (body.containsKey("defaultProduct")) {
            product.setDefaultProduct(asBool(body.get("defaultProduct"), false));
        } else if (creating) {
            product.setDefaultProduct(false);
        }
        if (body.containsKey("active")) {
            product.setActive(asBool(body.get("active"), true));
        } else if (creating) {
            product.setActive(true);
        }
        if (creating || body.containsKey("sortOrder")) {
            int fallback = creating ? 100 : (product.getSortOrder() == null ? 100 : product.getSortOrder());
            product.setSortOrder(asInt(body.get("sortOrder"), fallback));
        }
    }

    private List<PlatformProduct> catalogOrder(List<PlatformProduct> products) {
        return products.stream()
                .sorted(Comparator
                        .comparing((PlatformProduct item) -> !Boolean.TRUE.equals(item.getDefaultProduct()))
                        .thenComparing((item) -> item.getSortOrder() == null ? 100 : item.getSortOrder())
                        .thenComparing(PlatformProduct::getId))
                .toList();
    }

    private void clearOtherDefaults(Long keepId) {
        for (PlatformProduct product : productRepository.findAll()) {
            if (keepId != null && keepId.equals(product.getId())) {
                continue;
            }
            if (Boolean.TRUE.equals(product.getDefaultProduct())) {
                product.setDefaultProduct(false);
                productRepository.save(product);
            }
        }
    }

    private <E extends Enum<E>> E parseEnum(Object raw, Class<E> type, E fallback) {
        if (raw == null || String.valueOf(raw).isBlank()) {
            return fallback;
        }
        try {
            return Enum.valueOf(type, String.valueOf(raw).trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw OpsException.badRequest("Invalid " + type.getSimpleName() + ".");
        }
    }

    private String nameOf(Enum<?> value, Enum<?> fallback) {
        return (value == null ? fallback : value).name();
    }

    private String slug(String value) {
        if (value == null) {
            return "";
        }
        String slug = value.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-+$)", "");
        return slug.length() > 40 ? slug.substring(0, 40) : slug;
    }

    private String stringVal(Object value) {
        return value == null ? null : String.valueOf(value).trim();
    }

    private boolean asBool(Object value, boolean fallback) {
        if (value == null) {
            return fallback;
        }
        if (value instanceof Boolean bool) {
            return bool;
        }
        return "true".equalsIgnoreCase(String.valueOf(value));
    }

    private int asInt(Object value, int fallback) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value == null || String.valueOf(value).isBlank()) {
            return fallback;
        }
        return Integer.parseInt(String.valueOf(value));
    }

    private Long asLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value == null || String.valueOf(value).isBlank()) {
            return null;
        }
        return Long.parseLong(String.valueOf(value));
    }
}
