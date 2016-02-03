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

