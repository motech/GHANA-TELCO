package org.motechproject.ghana.mtn.process;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.motechproject.ghana.mtn.domain.MessageBundle;
import org.motechproject.ghana.mtn.domain.dto.SMSServiceRequest;
import org.motechproject.ghana.mtn.exception.MessageParseFailException;
import org.motechproject.ghana.mtn.parser.RelativeProgramMessageParser;
import org.motechproject.ghana.mtn.service.SMSService;
import org.motechproject.ghana.mtn.parser.CompositeInputMessageParser;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class UserMessageParserProcessorTest {
    private UserMessageParserProcess parserHandle;
    @Mock
    private SMSService smsService;
    @Mock
    private CompositeInputMessageParser inputParser;
    @Mock
    private MessageBundle messageBundle;
    @Mock
    private RelativeProgramMessageParser relativeProgramMessageHandler;

    @Before
    public void setUp() {
        initMocks(this);
        parserHandle = new UserMessageParserProcess(inputParser, relativeProgramMessageHandler, smsService, messageBundle);
    }

    @Test
    public void shouldSendSMSIfInputParserThrowsException() {
        String mobileNumber = "123";
        String errorMsg = "error";
        String input = "P 24";

        when(inputParser.parse(input, mobileNumber)).thenThrow(new MessageParseFailException(""));
        when(messageBundle.get(MessageBundle.ENROLLMENT_FAILURE)).thenReturn(errorMsg);

        parserHandle.process(mobileNumber, input);

        ArgumentCaptor<SMSServiceRequest> captor = ArgumentCaptor.forClass(SMSServiceRequest.class);
        verify(smsService).send(captor.capture());
        SMSServiceRequest captured = captor.getValue();
        assertEquals(mobileNumber,captured.getMobileNumber());
        assertEquals(errorMsg, captured.getMessage());
    }
}