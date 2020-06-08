package dataconsumerjava;

import dataconsumerjava.reader.processor.NewsRecordProcessorFactory;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.BasicConfigurator;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.kinesis.KinesisAsyncClient;
import software.amazon.kinesis.common.ConfigsBuilder;
import software.amazon.kinesis.common.KinesisClientUtil;
import software.amazon.kinesis.coordinator.Scheduler;

public class DataConsumerJavaApplication {

  private static final Log LOG = LogFactory.getLog(DataConsumerJavaApplication.class);

  private static final Logger ROOT_LOGGER = Logger.getLogger("");
  private static final Logger PROCESSOR_LOGGER =
      Logger.getLogger(
          "com.amazonaws.services.kinesis.samples.stocktrades.processor.StockTradeRecordProcessor");

  private static void setLogLevels() {
    ROOT_LOGGER.setLevel(Level.WARNING);
    // Set this to INFO for logging at INFO level. Suppressed for this example as it can be noisy.
    PROCESSOR_LOGGER.setLevel(Level.WARNING);
  }

  public static void main(String[] args) {
    Map<String, String> env = System.getenv();

    BasicConfigurator.configure();

    setLogLevels();

    String applicationName = env.get("GF_DSP_KNS_APPLICATION_NAME");
    String streamName = env.get("GF_DSP_KNS_STREAM_NAME");
    Region region = Region.of(env.get("GF_DSP_KNS_REGION"));

    if (region == null || streamName == null) {
      System.err.println(env.get("GF_DSP_KNS_REGION") + " is not a valid AWS region.");
      System.err.println("Stream name: " + env.get("GF_DSP_KNS_STREAM_NAME"));
      System.exit(1);
    }

    KinesisAsyncClient kinesisClient =
        KinesisClientUtil.createKinesisAsyncClient(KinesisAsyncClient.builder().region(region));
    DynamoDbAsyncClient dynamoClient = DynamoDbAsyncClient.builder().region(region).build();
    CloudWatchAsyncClient cloudWatchClient = CloudWatchAsyncClient.builder().region(region).build();
    NewsRecordProcessorFactory shardRecordProcessor = new NewsRecordProcessorFactory();
    ConfigsBuilder configsBuilder =
        new ConfigsBuilder(
            streamName,
            applicationName,
            kinesisClient,
            dynamoClient,
            cloudWatchClient,
            UUID.randomUUID().toString(),
            shardRecordProcessor);
    Scheduler scheduler =
        new Scheduler(
            configsBuilder.checkpointConfig(),
            configsBuilder.coordinatorConfig(),
            configsBuilder.leaseManagementConfig(),
            configsBuilder.lifecycleConfig(),
            configsBuilder.metricsConfig(),
            configsBuilder.processorConfig(),
            configsBuilder.retrievalConfig());
    int exitCode = 0;
    try {
      scheduler.run();
    } catch (Throwable t) {
      LOG.error("Caught throwable while processing data.", t);
      exitCode = 1;
    }
    System.exit(exitCode);
  }
}
