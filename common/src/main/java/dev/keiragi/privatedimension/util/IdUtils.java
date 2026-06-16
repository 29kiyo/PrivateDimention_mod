package dev.keiragi.privatedimension.util;
import java.lang.reflect.Method;
import java.util.Optional;
public final class IdUtils {
    private static final Method CREATE_ID_METHOD;
    private static final Method RESOURCE_KEY_CREATE_METHOD;
    private static final Method STRUCTURE_TEMPLATE_MANAGER_GET_METHOD;
    private static final Method REGISTRY_GET_VALUE_METHOD;
    static {
        CREATE_ID_METHOD = findCreateIdMethod();
        RESOURCE_KEY_CREATE_METHOD = findResourceKeyCreateMethod();
        STRUCTURE_TEMPLATE_MANAGER_GET_METHOD = findStructureTemplateManagerGetMethod();
        REGISTRY_GET_VALUE_METHOD = findRegistryGetValueMethod();
    }
    private IdUtils() {}
    public static Object createId(String namespace, String path) {
        try {
            return CREATE_ID_METHOD.invoke(null, namespace, path);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
    public static Object createResourceKey(Object registryKey, Object id) {
        try {
            return RESOURCE_KEY_CREATE_METHOD.invoke(null, registryKey, id);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
    @SuppressWarnings("unchecked")
    public static Optional<Object> getStructureTemplate(Object manager, Object id) {
        try {
            return (Optional<Object>) STRUCTURE_TEMPLATE_MANAGER_GET_METHOD.invoke(manager, id);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
    /** Registry.getValue(Identifier/ResourceLocation) をリフレクションで呼ぶ */
    public static Object registryGetValue(Object registry, String namespace, String path) {
        try {
            Object id = createId(namespace, path);
            return REGISTRY_GET_VALUE_METHOD.invoke(registry, id);
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }
    private static Method findCreateIdMethod() {
        try {
            Class<?> idClass = Class.forName("net.minecraft.resources.Identifier");
            return idClass.getMethod("fromNamespaceAndPath", String.class, String.class);
        } catch (ClassNotFoundException e) {
            try {
                Class<?> idClass = Class.forName("net.minecraft.resources.ResourceLocation");
                return idClass.getMethod("fromNamespaceAndPath", String.class, String.class);
            } catch (ReflectiveOperationException ex) {
                throw new RuntimeException(ex);
            }
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
    private static Method findResourceKeyCreateMethod() {
        try {
            Class<?> resourceKeyClass = Class.forName("net.minecraft.resources.ResourceKey");
            for (Method method : resourceKeyClass.getMethods()) {
                if (!"create".equals(method.getName()) || method.getParameterCount() != 2) continue;
                return method;
            }
            throw new RuntimeException("No ResourceKey.create method found");
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
    private static Method findStructureTemplateManagerGetMethod() {
        try {
            Class<?> managerClass = Class.forName("net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager");
            for (Method method : managerClass.getMethods()) {
                if (!"get".equals(method.getName()) || method.getParameterCount() != 1) continue;
                return method;
            }
            throw new RuntimeException("No StructureTemplateManager.get method found");
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
    private static Method findRegistryGetValueMethod() {
        try {
            Class<?> registryClass = Class.forName("net.minecraft.core.Registry");
            // Identifier/ResourceLocation を引数に取る getValue を探す
            for (Method method : registryClass.getMethods()) {
                if (!"getValue".equals(method.getName()) || method.getParameterCount() != 1) continue;
                String paramType = method.getParameterTypes()[0].getSimpleName();
                if (paramType.equals("Identifier") || paramType.equals("ResourceLocation")) {
                    return method;
                }
            }
            throw new RuntimeException("No Registry.getValue(Identifier) method found");
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
