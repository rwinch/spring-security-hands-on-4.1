package sample;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.springframework.security.test.context.support.WithUserDetails;

@WithUserDetails("eve@example.com")
@Retention(RetentionPolicy.RUNTIME)
public @interface WithEve {

}
