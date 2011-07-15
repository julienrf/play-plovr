package play.modules.plovr;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ExportClientSide {
    public String[] skip() default {}; /// Define the function parameters that should be skipped to compute the url
}