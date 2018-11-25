package com.reader.utility;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.api.LayoutComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.RootLoggerComponentBuilder;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;

public class LogsConfiguration {

    public static void setupLogging() {
        ConfigurationBuilder<BuiltConfiguration> builder = ConfigurationBuilderFactory.newConfigurationBuilder();

         AppenderComponentBuilder file = builder.newAppender("log",
         "File").addAttribute("fileName", "logs-reader.log");
        RootLoggerComponentBuilder rootLogger = builder.newRootLogger(Level.ALL);
        LayoutComponentBuilder standard = builder.newLayout("PatternLayout");
        standard.addAttribute("pattern", "%d [%t] %-5level: %msg%n%throwable");
         rootLogger.add(builder.newAppenderRef("log"));

//        AppenderComponentBuilder console = builder.newAppender("stdout", "Console");
//        builder.add(console);
//        console.add(standard);
//        rootLogger.add(builder.newAppenderRef("stdout"));
		file.add(standard);
		builder.add(file);
        builder.add(rootLogger);
        Configurator.initialize(builder.build());
    }

}
