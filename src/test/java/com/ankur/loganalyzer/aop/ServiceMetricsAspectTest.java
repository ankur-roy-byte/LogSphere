package com.ankur.loganalyzer.aop;

import com.ankur.loganalyzer.annotation.MetricCategory;
import com.ankur.loganalyzer.annotation.Tracked;
import com.ankur.loganalyzer.metrics.MetricsCollectorService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.annotation.Annotation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceMetricsAspectTest {

    @Mock private MetricsCollectorService metricsCollectorService;
    @Mock private ProceedingJoinPoint pjp;
    @Mock private Signature signature;

    @InjectMocks
    private ServiceMetricsAspect aspect;

    @BeforeEach
    void setUp() throws Throwable {
        when(pjp.getSignature()).thenReturn(signature);
        when(signature.toShortString()).thenReturn("TestService.testMethod()");
    }

    @Test
    void trackServiceMethod_ingestionCategory_recordsIngestionMetric() throws Throwable {
        when(pjp.proceed()).thenReturn("result");

        aspect.trackServiceMethod(pjp, tracked(MetricCategory.INGESTION));

        verify(metricsCollectorService).recordLogIngestion(anyLong(), eq(true));
        verifyNoMoreInteractions(metricsCollectorService);
    }

    @Test
    void trackServiceMethod_parsingCategory_recordsParsingMetric() throws Throwable {
        when(pjp.proceed()).thenReturn("result");

        aspect.trackServiceMethod(pjp, tracked(MetricCategory.PARSING));

        verify(metricsCollectorService).recordLogParsing(anyLong(), eq(true));
        verifyNoMoreInteractions(metricsCollectorService);
    }

    @Test
    void trackServiceMethod_analysisCategory_recordsAnalysisMetric() throws Throwable {
        when(pjp.proceed()).thenReturn("result");

        aspect.trackServiceMethod(pjp, tracked(MetricCategory.ANALYSIS));

        verify(metricsCollectorService).recordAnalysisExecution(anyLong(), eq(true));
        verifyNoMoreInteractions(metricsCollectorService);
    }

    @Test
    void trackServiceMethod_alertCategory_recordsAlertMetric() throws Throwable {
        when(pjp.proceed()).thenReturn(null);

        aspect.trackServiceMethod(pjp, tracked(MetricCategory.ALERT));

        verify(metricsCollectorService).recordAlertTriggered(eq(true));
        verifyNoMoreInteractions(metricsCollectorService);
    }

    @Test
    void trackServiceMethod_whenProceedThrows_recordsFailureAndRethrows() throws Throwable {
        RuntimeException cause = new RuntimeException("service failure");
        when(pjp.proceed()).thenThrow(cause);

        assertThatThrownBy(() -> aspect.trackServiceMethod(pjp, tracked(MetricCategory.ANALYSIS)))
                .isSameAs(cause);

        verify(metricsCollectorService).recordAnalysisExecution(anyLong(), eq(false));
    }

    @Test
    void trackServiceMethod_returnsProceedReturnValue() throws Throwable {
        Object expected = new Object();
        when(pjp.proceed()).thenReturn(expected);

        Object actual = aspect.trackServiceMethod(pjp, tracked(MetricCategory.INGESTION));

        assertThat(actual).isSameAs(expected);
    }

    private Tracked tracked(MetricCategory category) {
        return new Tracked() {
            @Override public Class<? extends Annotation> annotationType() { return Tracked.class; }
            @Override public MetricCategory category() { return category; }
            @Override public String operation() { return "test"; }
        };
    }
}
