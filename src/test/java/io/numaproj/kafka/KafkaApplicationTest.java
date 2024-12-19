package io.numaproj.kafka;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ConfigurableApplicationContext;

@Slf4j
@ExtendWith(MockitoExtension.class)
class KafkaApplicationTest {

    @Mock
    private ConfigurableApplicationContext configurableApplicationContextMock;

    @Test
    void main_initializeSuccess() throws Exception {
        /*
        Server kafkaSinkerServerMock = mock(Server.class);
        doReturn(kafkaSinkerServerMock).when(configurableApplicationContextMock).getBean(Server.class);
        try (MockedConstruction<SpringApplicationBuilder> ignored = Mockito
                .mockConstructionWithAnswer(SpringApplicationBuilder.class,
                        invoke -> configurableApplicationContextMock)
        ) {
            KafkaApplication.main(new String[]{"arg1", "arg2"});
            verify(kafkaSinkerServerMock, times(1)).start();
        }
         */
    }
}