package dataproducerjava.writer.controller;

import java.net.URI;
import java.util.concurrent.CompletableFuture;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kinesis.KinesisAsyncClient;
import software.amazon.awssdk.services.kinesis.model.CreateStreamRequest;
import software.amazon.awssdk.services.kinesis.model.ListStreamsResponse;
import software.amazon.awssdk.utils.AttributeMap;
import software.amazon.kinesis.common.KinesisClientUtil;

@RestController
public class DataProducerController {
  private static final Log LOG = LogFactory.getLog(DataProducerController.class);

  @RequestMapping("/feedData")
  public String pushData() {
    String streamName = "datascience-local-feed";
    String regionName = "us-east-2";
    Region region = Region.of(regionName);
    if (region == null) {
      System.err.println(regionName + " is not a valid AWS region.");
      System.exit(1);
    }
    URI myUri = URI.create("http://localhost:4568");
    LOG.info(myUri);
    LOG.info(AttributeMap.builder().toString());
    //    SdkAsyncHttpClient sdkAsyncHttpClient =
    //        NettyNioAsyncHttpClient.builder()
    //            .protocol(Protocol.HTTP1_1)
    //            .buildWithDefaults(
    //                AttributeMap.builder()
    //                    .put(SdkHttpConfigurationOption.TRUST_ALL_CERTIFICATES,
    // java.lang.Boolean.TRUE)
    //                    .build());
    //    NettyNioAsyncHttpClient.Builder httpClientBuilder = NettyNioAsyncHttpClient.builder();
    //    httpClientBuilder.buildWithDefaults(
    //        AttributeMap.builder()
    //            .put(SdkHttpConfigurationOption.TRUST_ALL_CERTIFICATES, Boolean.TRUE)
    //            .build());
    //    httpClientBuilder.buildWithDefaults(
    //        AttributeMap.builder().put(SdkHttpConfigurationOption.PROTOCOL,
    // Protocol.HTTP1_1).build());
    //    httpClientBuilder.protocol(Protocol.HTTP1_1);

    KinesisAsyncClient kinesisClient =
        KinesisClientUtil.createKinesisAsyncClient(
            KinesisAsyncClient.builder().region(region)
            //                .endpointOverride(myUri)
            //                .httpClientBuilder(httpClientBuilder)
            );
    LOG.info(kinesisClient);

    CompletableFuture<ListStreamsResponse> kac = kinesisClient.listStreams();
    try {
      LOG.info(kac.get().streamNames());
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return e.toString();
    }

    CreateStreamRequest r =
        CreateStreamRequest.builder().streamName(streamName).shardCount(1).build();
    kinesisClient.createStream(r);

    return "Hi";
  }
}
