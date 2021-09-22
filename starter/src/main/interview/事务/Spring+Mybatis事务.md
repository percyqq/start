DefaultTransactionDefinition def = new DefaultTransactionDefinition();
def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
TransactionStatus status = transactionManager.getTransaction(def);

try {
    // mapper invoke
    examinationMapper.updateById(examinationPo);
    //commit
    transactionManager.commit(status);
    System.out.println("done");
} catch (Exception e) {
    log.error(e.getMessage(), e);
    //rollback
    transactionManager.rollback(status);
}

事务提交
transactionManager.commit(status);


事务开始：
TransactionStatus status = transactionManager.getTransaction(def);

============================================================================================================
additional 场景：
对于 REQUIRES_NEW 要刮起当前事务。
PROPAGATION_REQUIRES_NEW
    // unbind
    // 1.
    SqlSessionUtils.suspend() 
        TransactionSynchronizationManager.unbindResource(this.sessionFactory);
    // 2.
    DataSourceTransactionManager.doSuspend
        TransactionSynchronizationManager.unbindResource(obtainDataSource());
============================================================================================================

！！！事务开始！！！
    //bind 操作
    ① Spring事务管理：
    DataSourceTransactionManager.doBegin
        // Bind the connection holder to the thread.
        if (txObject.isNewConnectionHolder()) {
            TransactionSynchronizationManager.bindResource(obtainDataSource(), txObject.getConnectionHolder());
        }
↓
↓
    ② mapper invoke   当遇到mapper执行。开始绑定SQLSession相关。 即 getSqlSession() 获取代理mapper时 
    SqlSessionUtils.registerSessionHolder
        holder = new SqlSessionHolder(session, executorType, exceptionTranslator);
        TransactionSynchronizationManager.bindResource(sessionFactory, holder);
↓
    执行sql，完成。
↓
↓
↓
    // unbind     由transactionManager.commit(status); 触发
        triggerBeforeCompletion
            TransactionSynchronizationUtils.triggerBeforeCompletion();
            ==>  B1
        processCommit        
            ==>  B2
.
    a》
    AbstractPlatformTransactionManager.triggerBeforeCompletion
        TransactionSynchronizationUtils.triggerBeforeCompletion();
            TransactionSynchronizationManager.getSynchronizations().foreach( synchronization -> synchronization.beforeCompletion() )
.
                synchronization   ==>  SqlSessionUtils 中的内部类： SqlSessionSynchronization
                TransactionSynchronizationAdapter implements TransactionSynchronization
                private static final class SqlSessionSynchronization extends TransactionSynchronizationAdapter
                SqlSessionSynchronization.beforeCompletion()
.    
    b》
    finally {
        AbstractPlatformTransactionManager.cleanupAfterCompletion(status);
    }
.
.
    B1             
    SqlSessionUtils.beforeCompletion()
        TransactionSynchronizationManager.unbindResource(sessionFactory);
    B2
    DataSourceTransactionManager.doCleanupAfterCompletion()
        if (txObject.isNewConnectionHolder()) {
            TransactionSynchronizationManager.unbindResource(obtainDataSource());
        }    








