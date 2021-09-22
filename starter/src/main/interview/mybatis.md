https://www.cnblogs.com/ChenLLang/p/5307590.html

mybatis如何根据mapper接口生成其实现类
    mybatis通过JDK的动态代理方式，在启动加载配置文件时，根据配置mapper的xml去生成。

mybatis里头给sqlSession指定执行哪条sql的时候，有两种方式，一种是写mapper的xml的namespace+statementId，如下：
public Student findStudentById(Integer studId) {
    SqlSession sqlSession = MyBatisSqlSessionFactory.getSqlSession();
    try {
        //方案1
        StudentMapper studentMapper = sqlSession.getMapper(StudentMapper.class);
        return studentMapper.findStudentById(studId);
    ----------------------------------------------------------------------------------------------
        //方案2
        return sqlSession.selectOne("com.mybatis3.StudentMapper.findStudentById", studId);
    } finally {
        sqlSession.close();
    }
}

@SuppressWarnings("unchecked")
protected T newInstance(MapperProxy<T> mapperProxy) {
    return (T) Proxy.newProxyInstance(mapperInterface.getClassLoader(), new Class[] { mapperInterface }, mapperProxy);
}

public T newInstance(SqlSession sqlSession) {
    final MapperProxy<T> mapperProxy = new MapperProxy<T>(sqlSession, mapperInterface, methodCache);
    return newInstance(mapperProxy);
}

这里使用 MapperFactoryBean将Mapper接口配置成 Spring bean 实体同时注入sqlSessionFactory。




