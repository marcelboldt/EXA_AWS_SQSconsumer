package com.exasol.aws;

import java.util.List;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.*;

import com.exasol.*;

public class SQSconsumer {

    static AWSCredentialsProvider credentialsProvider;
    static AmazonSQS sqs;

    public static void init(ExaMetadata exa) throws Exception {
        credentialsProvider = new DefaultAWSCredentialsProviderChain();

        try {
            AWSCredentials credentials = credentialsProvider.getCredentials();

        } catch (Exception e) {
            throw new ExaConnectionAccessException(
                    "Cannot load the AWS credentials."
            );
        }

        AmazonSQSClientBuilder sqsClientBuilder = AmazonSQSClientBuilder.standard()
                .withRegion(Regions.EU_WEST_1)
                .withCredentials(credentialsProvider);
        sqs = sqsClientBuilder.build();
    }


    public static void cleanup(ExaMetadata exa) {
        sqs.shutdown();
    }

    public static void run(ExaMetadata exa, ExaIterator ctx) throws Exception {
        /* queue_name String
        *  max_msgs Int
        *
         */
//        List<Message> messages = SQSread();

        for (Message m : SQSread(ctx.getString("queue_name"), ctx.getInteger("max_msgs"))) {
            ctx.emit(m.getMessageId(), m.getBody(), m.getAttributes().toString());
        }
    }

    public static List<Message> SQSread(String queueName, int max_msg) {

        List<Message> messages = null;

        try {
            GetQueueUrlResult myQueueUrlResult = sqs.getQueueUrl(queueName);
            String myQueueUrl = myQueueUrlResult.getQueueUrl();

            // Receive messages
            ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(myQueueUrl);
            receiveMessageRequest.setMaxNumberOfMessages(max_msg); // default is 1; valid values 1-10
            messages = sqs.receiveMessage(receiveMessageRequest).getMessages();


            // Delete messages

            for (int i = 0; i < messages.size(); i++) {
                String messageReceiptHandle = messages.get(i).getReceiptHandle();
                sqs.deleteMessage(new DeleteMessageRequest(myQueueUrl, messageReceiptHandle));
            }



        } catch (AmazonServiceException ase) {
            System.out.println("Caught an AmazonServiceException, which means your request made it " +
                    "to Amazon SQS, but was rejected with an error response for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException, which means the client encountered " +
                    "a serious internal problem while trying to communicate with SQS, such as not " +
                    "being able to access the network.");
            System.out.println("Error Message: " + ace.getMessage());
        }

        return messages;
    }
}
