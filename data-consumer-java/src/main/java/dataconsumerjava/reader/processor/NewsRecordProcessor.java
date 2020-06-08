/*
 * Copyright 2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Amazon Software License (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 * http://aws.amazon.com/asl/
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package dataconsumerjava.reader.processor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import software.amazon.kinesis.exceptions.InvalidStateException;
import software.amazon.kinesis.exceptions.ShutdownException;
import software.amazon.kinesis.exceptions.ThrottlingException;
import software.amazon.kinesis.lifecycle.events.InitializationInput;
import software.amazon.kinesis.lifecycle.events.LeaseLostInput;
import software.amazon.kinesis.lifecycle.events.ProcessRecordsInput;
import software.amazon.kinesis.lifecycle.events.ShardEndedInput;
import software.amazon.kinesis.lifecycle.events.ShutdownRequestedInput;
import software.amazon.kinesis.processor.RecordProcessorCheckpointer;
import software.amazon.kinesis.processor.ShardRecordProcessor;
import software.amazon.kinesis.retrieval.KinesisClientRecord;

/** Processes records retrieved from stock trades stream. */
public class NewsRecordProcessor implements ShardRecordProcessor {

  private static final Log log = LogFactory.getLog(NewsRecordProcessor.class);

  private String kinesisShardId;

  // Reporting interval
  private static final long REPORTING_INTERVAL_MILLIS = 60000L; // 1 minute
  private long nextReportingTimeInMillis;

  // Checkpointing interval
  private static final long CHECKPOINT_INTERVAL_MILLIS = 60000L; // 1 minute
  private long nextCheckpointTimeInMillis;

  @Override
  public void initialize(InitializationInput initializationInput) {
    kinesisShardId = initializationInput.shardId();
    log.info("Initializing record processor for shard: " + kinesisShardId);
    log.info("Initializing @ Sequence: " + initializationInput.extendedSequenceNumber().toString());

    nextReportingTimeInMillis = System.currentTimeMillis() + REPORTING_INTERVAL_MILLIS;
    nextCheckpointTimeInMillis = System.currentTimeMillis() + CHECKPOINT_INTERVAL_MILLIS;
  }

  @Override
  public void processRecords(ProcessRecordsInput processRecordsInput) {
    // TODO: Implement method

  }

  private void reportStats() {
    // TODO: Implement method
  }

  private void resetStats() {
    // TODO: Implement method
  }

  private void processRecord(KinesisClientRecord record) {
    byte[] arr = new byte[record.data().remaining()];
    record.data().get(arr);
    String news = new String(arr);
    if (news != null && news.isEmpty()) {
      log.info(news);
    } else {
      log.warn(
          "Skipping record. Unable to parse record into StockTrade. Partition Key: "
              + record.partitionKey());
      return;
    }
  }

  @Override
  public void leaseLost(LeaseLostInput leaseLostInput) {
    log.info("Lost lease, so terminating.");
  }

  @Override
  public void shardEnded(ShardEndedInput shardEndedInput) {
    try {
      // Important to checkpoint after reaching end of shard, so we can start processing data from
      // child shards.
      log.info("Reached shard end checkpointing.");
      shardEndedInput.checkpointer().checkpoint();
    } catch (ShutdownException | InvalidStateException e) {
      log.error("Exception while checkpointing at shard end. Giving up.", e);
    }
  }

  @Override
  public void shutdownRequested(ShutdownRequestedInput shutdownRequestedInput) {
    log.info("Scheduler is shutting down, checkpointing.");
    checkpoint(shutdownRequestedInput.checkpointer());
  }

  private void checkpoint(RecordProcessorCheckpointer checkpointer) {
    log.info("Checkpointing shard " + kinesisShardId);
    try {
      checkpointer.checkpoint();
    } catch (ShutdownException se) {
      // Ignore checkpoint if the processor instance has been shutdown (fail over).
      log.info("Caught shutdown exception, skipping checkpoint.", se);
    } catch (ThrottlingException e) {
      // Skip checkpoint when throttled. In practice, consider a backoff and retry policy.
      log.error("Caught throttling exception, skipping checkpoint.", e);
    } catch (InvalidStateException e) {
      // This indicates an issue with the DynamoDB table (check for table, provisioned IOPS).
      log.error(
          "Cannot save checkpoint to the DynamoDB table used by the Amazon Kinesis Client Library.",
          e);
    }
  }
}
