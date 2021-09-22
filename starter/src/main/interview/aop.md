
AOP顺序无所谓， 主要是
try  catch之后，一定要throw异常供后面的处理，否则就会失效。



org.springframework.transaction.interceptor.TransactionAspectSupport

protected Object invokeWithinTransaction(Method method, Class<?> targetClass, final InvocationCallback invocation)
			throws Throwable {
		// If the transaction attribute is null, the method is non-transactional.
		final TransactionAttribute txAttr = getTransactionAttributeSource().getTransactionAttribute(method, targetClass);
		final PlatformTransactionManager tm = determineTransactionManager(txAttr);
		final String joinpointIdentification = methodIdentification(method, targetClass, txAttr);
//
		if (txAttr == null || !(tm instanceof CallbackPreferringPlatformTransactionManager)) {
			// Standard transaction demarcation with getTransaction and commit/rollback calls.
			TransactionInfo txInfo = createTransactionIfNecessary(tm, txAttr, joinpointIdentification);
			Object retVal = null;
			try {
				// This is an around advice: Invoke the next interceptor in the chain.
				// This will normally result in a target object being invoked.
				retVal = invocation.proceedWithInvocation();
			}
			catch (Throwable ex) {
				// target invocation exception
				completeTransactionAfterThrowing(txInfo, ex);
[===>]			throw ex;
			}
			finally {
				cleanupTransactionInfo(txInfo);
			}
			commitTransactionAfterReturning(txInfo);
			return retVal;
		}
		...
}

@Component
@Aspect
@Slf4j
@Order(5)
public class JwtAop {
    @Pointcut("execution(public * com.web.test.*.*(..))")
    public void lockPointcut1() {}
    //
    @Around("lockPointcut1()")
    public Object around1(ProceedingJoinPoint point) throws Throwable {
        Object object = null;
        try {
            object = point.proceed(point.getArgs());
        } catch (Exception e) {
            e.printStackTrace();
            ===> 
        } finally {
            System.out.println(11);
        }
        return object;
    }
}