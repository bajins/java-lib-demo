package com.bajins.demo.mybatis;

public class MybatisLearning {

    /**
     * 批处理insert数据
     *
     * @param mapperClass
     * @param list
     * @return
     */
    /*public <T> int batchInsertAll(Class<? extends BaseDao<T>> mapperClass, List<T> list) {
        int count = 0;
        if (null == mapperClass || null == list || list.size() <= 0) {
            return count;
        }
        try (SqlSession session = sqlSessionFactory.openSession(ExecutorType.BATCH, false);) {
            BaseDao<T> baseDao = session.getMapper(mapperClass);
            Lists.partition(list, 1000).forEach(batchs -> { // 每次插入最多1000条数据
                batchs.forEach(batch -> {
                    baseDao.insertNotNull(batch);
                });
                session.commit(true);
            });
            count = list.size();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw e;
        }
        return count;
    }*/


    public static void main(String[] args) {

    }
}
