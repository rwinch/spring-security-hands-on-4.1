package sample.config;

import java.util.List;

import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.serializer.DefaultDeserializer;
import org.springframework.core.serializer.support.DeserializingConverter;
import org.springframework.core.serializer.support.SerializingConverter;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.data.rest.core.event.ValidatingRepositoryEventListener;
import org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.web.method.annotation.CsrfTokenArgumentResolver;
import org.springframework.session.Session;
import org.springframework.session.SessionRepository;
import org.springframework.session.web.http.SessionRepositoryFilter;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.Validator;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.filter.RequestContextFilter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

@Configuration
class DataRestConfig extends RepositoryRestMvcConfiguration {
	@Autowired
	ApplicationContext context;

	@Override
	protected void configureValidatingRepositoryEventListener(ValidatingRepositoryEventListener listener) {
		Validator validator = context.getBean("mvcValidator", Validator.class);
		listener.addValidator("beforeSave", validator);
		listener.addValidator("beforeCreate", validator);
	}
}

@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
class MvcConfig extends WebMvcConfigurerAdapter {

	@Autowired
	@Qualifier("repositoryExporterHandlerAdapter")
	RequestMappingHandlerAdapter repositoryExporterHandlerAdapter;

	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
		List<HandlerMethodArgumentResolver> customArgumentResolvers = repositoryExporterHandlerAdapter
				.getCustomArgumentResolvers();
		argumentResolvers.add(new CsrfTokenArgumentResolver());
		argumentResolvers.addAll(customArgumentResolvers);
	}
}

/**
 * This is to work around
 * https://github.com/spring-projects/spring-boot/issues/3805
 *
 * @author Rob Winch
 *
 */
@Configuration
class SpringSessionConfig {

	@Bean
	public RedisSerializer<Object> defaultRedisSerializer() {
		return new BeanClassLoaderAwareJdkRedisSerializer();
	}

	static class BeanClassLoaderAwareJdkRedisSerializer implements RedisSerializer<Object>, BeanClassLoaderAware {
		private Converter<Object, byte[]> serializer = new SerializingConverter();
		private Converter<byte[], Object> deserializer;

		@Override
		public void setBeanClassLoader(ClassLoader classLoader) {
			this.deserializer = new DeserializingConverter(new DefaultDeserializer(classLoader));
		}

		public Object deserialize(byte[] bytes) {
			if (ObjectUtils.isEmpty(bytes)) {
				return null;
			}

			try {
				Object result = deserializer.convert(bytes);
				return result;
			} catch (Exception ex) {
				throw new SerializationException("Cannot deserialize", ex);
			}
		}

		public byte[] serialize(Object object) {
			if (object == null) {
				return new byte[0];
			}
			try {
				return serializer.convert(object);
			} catch (Exception ex) {
				throw new SerializationException("Cannot serialize", ex);
			}
		}

		@Bean
		public AuthenticationSuccessEventListener asel() {
			return new AuthenticationSuccessEventListener();
		}

		@Bean
		public OrderedRequestContextFilter requestContextFilter() {
			return new OrderedRequestContextFilter();
		}

		@Order(SessionRepositoryFilter.DEFAULT_ORDER + 10)
		static class OrderedRequestContextFilter extends RequestContextFilter {
		}

		@Component
		static class AuthenticationSuccessEventListener implements ApplicationListener<AuthenticationSuccessEvent> {
			@Autowired(required=false)
			SessionRepository<? extends Session> sessions;

			@Override
			public void onApplicationEvent(AuthenticationSuccessEvent ase) {
				String username = ase.getAuthentication().getName();

				RequestAttributes attributes = RequestContextHolder.getRequestAttributes();

				attributes.setAttribute(Session.PRINCIPAL_NAME_ATTRIBUTE_NAME, username,
						RequestAttributes.SCOPE_SESSION);
			}

		}

	}
}
