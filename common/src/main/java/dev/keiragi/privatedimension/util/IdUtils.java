package dev.keiragi.privatedimension.util;

import java.lang.reflect.Method;
import java.util.Optional;

public final class IdUtils {
    private static final Method CREATE_ID_METHOD;
    private static final Method RESOURCE_KEY_CREATE_METHOD;
    private static final Method STRUCTURE_TEMPLATE_MANAGER_GET_METHOD;

    static {
        CREATE_ID_METHOD = findCreateIdMethod();
        RESOURCE_KEY_CREATE_METHOD = findResourceKeyCreateMethod();
        STRUCTURE_TEMPLATE_MANAGER_GET_METHOD = findStructureTemplateManagerGetMethod();
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
}
