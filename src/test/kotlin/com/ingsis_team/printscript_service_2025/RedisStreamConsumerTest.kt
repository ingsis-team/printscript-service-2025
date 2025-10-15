package com.ingsis_team.printscript_service_2025
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.connection.stream.ObjectRecord
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.stream.StreamReceiver
import com.ingsis_team.printscript_service_2025.redis.consumer.RedisStreamConsumer
import java.time.Duration
import kotlin.test.assertEquals

// Concrete test class
class TestRedisStreamConsumer : RedisStreamConsumer<String>("test-stream", "test-group", DummyRedisTemplate()) {
    var lastProcessedRecord: String? = null

    public override fun onMessage(record: ObjectRecord<String, String>) {
        lastProcessedRecord = record.value
        println("Processing record: ${record.value}")
    }

    override fun options(): StreamReceiver.StreamReceiverOptions<String, ObjectRecord<String, String>> {
        return StreamReceiver.StreamReceiverOptions.builder()
            .pollTimeout(Duration.ofMillis(100))
            .targetType(String::class.java)
            .build()
    }
}

// Dummy RedisTemplate for basic purposes
class DummyRedisTemplate : ReactiveRedisTemplate<String, String>(
    mock(ReactiveRedisConnectionFactory::class.java),
    RedisSerializationContext.string(),
)

class RedisStreamConsumerTest {
    @Test
    fun `test onMessage execution`() {
        val consumer = TestRedisStreamConsumer()
        val dummyRecord = ObjectRecord.create("test-stream", "test-message")
        consumer.onMessage(dummyRecord)
        assertEquals("test-message", consumer.lastProcessedRecord)
        println("onMessage executed successfully")
    }
}