package net.whydah.admin.config;

import jakarta.inject.Singleton;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.ext.Provider;
import java.lang.annotation.Annotation;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.process.internal.RequestScoped;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

@Provider
@Component
public class JerseyBindings extends AbstractBinder {
    
    // Static reference to Spring context - will be populated by Spring
    private static ApplicationContext springContext;
    
    // Default constructor for HK2
    public JerseyBindings() {
        // This constructor will be used by HK2
    }
    
    // Set the Spring context
    @Autowired
    public void setSpringContext(ApplicationContext context) {
        JerseyBindings.springContext = context;
    }
    
    @Override
    protected void configure() {
        if (springContext == null) {
            System.err.println("ERROR: Spring context not available to JerseyBindings");
            return;
        }
        
        // Automatically register all Spring beans with JAX-RS @Path annotation
        registerSpringBeansWithAnnotation(Path.class, RequestScoped.class);
        
        // Optionally register other types of beans you need in Jersey
        registerSpringBeansWithAnnotation(Service.class, Singleton.class);
        registerSpringBeansWithAnnotation(Repository.class, Singleton.class);
    }
    
    /**
     * Register all Spring beans with a specific annotation to HK2
     */
    private <A extends Annotation> void registerSpringBeansWithAnnotation(
            Class<A> annotationClass, Class<? extends Annotation> scope) {
        
        // Get all bean names with the given annotation
        String[] beanNames = springContext.getBeanNamesForAnnotation(annotationClass);
        
        for (String beanName : beanNames) {
            Class<?> beanClass = springContext.getType(beanName);
            if (beanClass != null) {
                registerBean(beanClass, scope);
                
                System.out.println("Registered Spring bean: " + beanClass.getName() + 
                    " with scope: " + scope.getSimpleName());
            }
        }
    }
    
    /**
     * Register a specific bean
     */
    private <T> void registerBean(Class<T> beanClass, Class<? extends Annotation> scope) {
        bindFactory(new SpringManagedBeanFactory<>(beanClass))
            .to(beanClass)
            .in(scope);
    }
    
    /**
     * Generic factory for creating Spring-managed beans
     */
    private static class SpringManagedBeanFactory<T> implements org.glassfish.hk2.api.Factory<T> {
        private final Class<T> beanClass;
        
        public SpringManagedBeanFactory(Class<T> beanClass) {
            this.beanClass = beanClass;
        }
        
        @Override
        @SuppressWarnings("unchecked")
        public T provide() {
            if (springContext == null) {
                throw new IllegalStateException("Spring context not available");
            }
            return (T) springContext.getBean(beanClass);
        }
        
        @Override
        public void dispose(T instance) {
            // Spring handles bean disposal
        }
    }
}