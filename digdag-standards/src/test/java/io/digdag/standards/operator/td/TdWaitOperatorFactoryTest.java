package io.digdag.standards.operator.td;

import com.google.common.base.Optional;
import com.treasuredata.client.model.TDJobRequest;
import io.digdag.client.config.Config;
import io.digdag.util.BaseOperator;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.nio.file.Path;
import java.nio.file.Paths;

import static io.digdag.client.config.ConfigUtils.newConfig;
import static io.digdag.core.workflow.OperatorTestingUtils.*;
import static io.digdag.standards.operator.td.TdOperatorFactory.insertCommandStatement;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TdWaitOperatorFactoryTest
{
    @Mock
    TDOperator op;

    /**
     * Check config parameters are set to TDJobRequest
     */
    @Test
    public void testTDJobRequestParams()
            throws Exception
    {
        Path projectPath = Paths.get("").normalize().toAbsolutePath();

        Config config = newConfig()
                .set("database", "testdb")
                .set("query", "select 1")
                .set("engine", "presto");

        when(op.submitNewJobWithRetry(any(TDJobRequest.class))).thenReturn("");
        when(op.getDatabase()).thenReturn("testdb");

        TDJobRequest jobRequest = testTDJobRequestParams(projectPath, config);

        assertEquals("testdb", jobRequest.getDatabase());
        assertEquals("select 1", jobRequest.getQuery());
        assertEquals("presto", jobRequest.getType().toString());
        assertEquals(Optional.absent(), jobRequest.getEngineVersion());

    }

    /**
     * Check config parameters are set to TDJobRequest with engine_version
     */
    @Test
    public void testTDJobRequestParamsWithEngineVersion()
            throws Exception
    {
        Path projectPath = Paths.get("").normalize().toAbsolutePath();

        Config config = newConfig()
                .set("database", "testdb")
                .set("query", "select 1")
                .set("engine", "hive")
                .set("engine_version", "stable");

        when(op.submitNewJobWithRetry(any(TDJobRequest.class))).thenReturn("");
        when(op.getDatabase()).thenReturn("testdb");

        TDJobRequest jobRequest = testTDJobRequestParams(projectPath, config);

        assertEquals("testdb", jobRequest.getDatabase());
        assertEquals("select 1", jobRequest.getQuery());
        assertEquals("hive", jobRequest.getType().toString());
        assertTrue(jobRequest.getEngineVersion().isPresent());
        assertEquals("stable", jobRequest.getEngineVersion().get().toString());

    }

    private TDJobRequest testTDJobRequestParams(Path projectPath, Config config)
    {
        ArgumentCaptor<TDJobRequest> captor = ArgumentCaptor.forClass(TDJobRequest.class);

        TdWaitOperatorFactory.TdWaitOperator operator =
                (TdWaitOperatorFactory.TdWaitOperator) newOperatorFactory(TdWaitOperatorFactory.class)
                        .newOperator(newContext(projectPath, newTaskRequest().withConfig(config)));

        operator.startJob(op, "");

        Mockito.verify(op).submitNewJobWithRetry(captor.capture());
        TDJobRequest jobRequest = captor.getValue();
        return jobRequest;
    }
}