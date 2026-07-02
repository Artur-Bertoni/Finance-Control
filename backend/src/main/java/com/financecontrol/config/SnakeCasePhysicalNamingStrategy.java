package com.financecontrol.config;

import org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

@SuppressWarnings("removal")
public class SnakeCasePhysicalNamingStrategy extends CamelCaseToUnderscoresNamingStrategy {

    @Override
    public Identifier toPhysicalTableName(Identifier name, JdbcEnvironment context) {
        return reapplyQuoting(name, super.toPhysicalTableName(unquoted(name), context));
    }

    @Override
    public Identifier toPhysicalColumnName(Identifier name, JdbcEnvironment context) {
        return reapplyQuoting(name, super.toPhysicalColumnName(unquoted(name), context));
    }

    @Override
    public Identifier toPhysicalSequenceName(Identifier name, JdbcEnvironment context) {
        return reapplyQuoting(name, super.toPhysicalSequenceName(unquoted(name), context));
    }

    private static Identifier unquoted(Identifier id) {
        return id == null ? null : Identifier.toIdentifier(id.getText(), false);
    }

    private static Identifier reapplyQuoting(Identifier original, Identifier converted) {
        if (converted == null) return null;
        return Identifier.toIdentifier(converted.getText(), original != null && original.isQuoted());
    }
}
