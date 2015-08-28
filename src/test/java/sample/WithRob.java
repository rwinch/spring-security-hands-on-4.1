package sample;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.springframework.security.test.context.support.WithUserDetails;

@WithUserDetails("rob@example.com")
@Retention(RetentionPolicy.RUNTIME)
public @interface WithRob {

}
