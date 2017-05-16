create schema load;
CREATE OR REPLACE JAVA SET SCRIPT load."SQSCONSUMER"("queue_name" VARCHAR(100) UTF8, "max_msgs" DECIMAL(18,0)) EMITS("MESSAGE_ID" VARCHAR(100) UTF8, "MSG_BODY" VARCHAR(2000000) UTF8, "MSG_ATTRIBUTES" VARCHAR(2000000) UTF8) AS
%jvmoption -Xms128m -Xmx128m;
%jar /buckets/bfsdefault/jars/SQSconsumer.jar;
%scriptclass com.exasol.aws.SQSconsumer;
/

select load.sqsconsumer('mbo_test1', 10);