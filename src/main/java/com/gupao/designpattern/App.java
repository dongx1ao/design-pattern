package com.gupao.designpattern;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletComponentScan;

/**
 * Hello world!
 *
 */
@SpringBootApplication
@ServletComponentScan(basePackages = {"com.gupao.*"})
public class App 
{
    public static void main( String[] args )
    {
        new SpringApplicationBuilder(App.class).run(args);
    }
}
