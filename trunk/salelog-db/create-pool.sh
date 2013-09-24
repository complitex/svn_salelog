#!/bin/sh

GLASSFISH_ASADMIN=asadmin

echo ---------------------------------------------------
echo Local database and Realm
echo ---------------------------------------------------
echo
echo Register the JDBC connection pool
$GLASSFISH_ASADMIN create-jdbc-connection-pool --datasourceclassname="com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource" --restype="javax.sql.ConnectionPoolDataSource" --property="url=jdbc\:mysql\://localhost\:3306/salelog:user=salelog:password=salelog:characterResultSets=utf8:characterEncoding=utf8:useUnicode=true" salelogPool

echo
echo Create a JDBC resource with the specified JNDI name
$GLASSFISH_ASADMIN create-jdbc-resource --connectionpoolid=salelogPool jdbc/salelogResource

echo
echo Add the named authentication realm
$GLASSFISH_ASADMIN create-auth-realm --classname="com.sun.enterprise.security.auth.realm.jdbc.JDBCRealm" --property="jaas-context=jdbcRealm:datasource-jndi=jdbc/salelogResource:user-table=user:user-name-column=login:password-column=password:group-table=usergroup:group-name-column=group_name:charset=UTF-8:digest-algorithm=MD5" salelogRealm

