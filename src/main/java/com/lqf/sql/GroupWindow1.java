package com.lqf.sql;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;

public class GroupWindow1 {
    public static void main(String[] args) {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        StreamTableEnvironment tEnv = StreamTableEnvironment.create(env);

// 作为事件时间的字段必须是 timestamp 类型, 所以根据 long 类型的 ts 计算出来一个 t
        tEnv.executeSql("create table sensor(" +
                "id string," +
                "ts bigint," +
                "vc int, " +
                "t as to_timestamp(from_unixtime(ts/1000,'yyyy-MM-dd HH:mm:ss'))," +
                "watermark for t as t - interval '5' second)" +
                "with("
                + "'connector' = 'filesystem',"
                + "'path' = 'input/sensor.txt',"
                + "'format' = 'csv'"
                + ")");

        tEnv
                .sqlQuery(
                        "SELECT id, " +
                                "  TUMBLE_START(t, INTERVAL '1' minute) as wStart,  " +
                                "  TUMBLE_END(t, INTERVAL '1' minute) as wEnd,  " +
                                "  SUM(vc) sum_vc " +
                                "FROM sensor " +
                                "GROUP BY TUMBLE(t, INTERVAL '1' minute), id"
                )
                .execute()
                .print();
    }
}
