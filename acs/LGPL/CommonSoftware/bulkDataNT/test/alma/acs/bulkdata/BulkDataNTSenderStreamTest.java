/*
* ALMA - Atacama Large Millimiter Array
* Copyright (c) National Astronomical Observatory of Japan, 2017 
* 
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
* 
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
* 
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
*/
package alma.acs.bulkdata;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * This JUnit test checks the behavior of BulkDataNTSenderStream.
 *
 * @author Takashi Nakamoto
 */
public class BulkDataNTSenderStreamTest {
    @Before
    public void setUp()
        throws FailedToGetQosException,
               FailedToSetQosException,
               NoQosXmlInAcsEnvironmentException,
               CannotLoadQosXmlSchemaException,
               CannotReadQosXmlException,
               InvalidQosXmlException {
        BulkDataNTGlobalConfiguration.getInstance().loadQosXml();
	}
	
    @Test
    public void testEmptyStream()
        throws DuplicatedSenderStreamNameException, 
               InvalidStreamNameException,
               QosXmlNotLoadedException,
               QosLibraryNotExistException,
               QosProfileNotExistException,
               IllegalDomainIdException,
               UndestroyedSenderFlowsException,
               DdsException {
        // Precondition: create a new Stream without any Flow
        String streamName = "EmptyStream";
        BulkDataNTSenderStream stream
            = new BulkDataNTSenderStream(streamName,
                                         new BulkDataNTSenderStreamConfigurationDefault());
        
        // Evaluation: check the behavior of instance methods.
        assertEquals(streamName, stream.getName());
        assertEmptySenderStream(stream);

        // Post process: destroy the stream
        stream.destroy();
    }

    @Test
    public void testMultipleStreams() {
        // TODO: test illegal and legal stream names.
    }

    @Test
    @Ignore("This test takes long time when destroying stream.")
    public void testValidStreamNames()
        throws InvalidStreamNameException,
               QosXmlNotLoadedException,
               DdsException,
               IllegalConfigurationException {
        try{
            String validStreamNames[]
                = new String[] { // Stream name with a single character
                "0", "9", "a", "z", "A", "Z", "!",
                // Stream name that consists of 127 characters.
                // This includes all valid characters (all ASCII-printable
                // characters except white space and #).
                "!\"$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~abcdefghijklmnopqrstuvwxyz01234567"
            };

            BulkDataNTSenderStreamConfiguration conf
                = new BulkDataNTSenderStreamConfigurationDefault();

            for (String name: validStreamNames) {
                BulkDataNTSenderStream stream
                    = new BulkDataNTSenderStream(name, conf);
                stream.destroy();
            }
                   
        } catch (InvalidStreamNameException ex) {
            fail("Valid stream name is judged as illegal.");
        } catch (Exception ex) {
            fail("Unexpected exception happened during the test.");
        }
    }

    @Test
    public void testInvalidStreamNames()
        throws QosXmlNotLoadedException,
               DdsException,
               IllegalDomainIdException,
               IllegalConfigurationException {
        String invalidStreamNames[]
            = new String[] { null, "",
                             "#", "#ab", "ab#", "a#b", // contains '#'
                             " ", " ab", "ab ", "a b", // contains ' '
                             "\b", "\t", "\n", "\f", "\r", // non-printable
                             "\u4E2D", "\u672C", "\u5D07", "\u5FD7", // non-ASCII
                             "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcd" // 128 characters
        };

        BulkDataNTSenderStreamConfiguration conf
            = new BulkDataNTSenderStreamConfigurationDefault();

        for (String name: invalidStreamNames) {
            try {
                new BulkDataNTSenderStream(name, conf);
                fail("InvalidStreamNameException must happen, but did not.");
            } catch (InvalidStreamNameException ex) {
                assertNotNull(ex.getMessage());
                assertFalse(ex.getMessage().isEmpty());
            } catch (Exception ex) {
                fail("InvalidStreamNameException was expected, but differnt " +
                     "type of exception was thrown.");
            }
        }
    }

    @Test
    public void testDuplicatedStreamName() {
        // TODO: test what happens when two streams are created with the same
        //       name.
    }

    @Test
    public void testDuplicatedFlowName() {
        // TODO: test what happens when two flows are created with the same
        //       name.
    }

    @Test
    public void testOneFlow()
        throws InvalidStreamNameException,
               QosXmlNotLoadedException,
               DdsException,
               IllegalConfigurationException,
               InvalidFlowNameException,
               FlowNotExistException,
               DuplicatedFlowNameException,
               QosLibraryNotExistException,
               QosProfileNotExistException,
               StreamAlreadyDestroyedException,
               IllegalDomainIdException,
               InappropriateSenderFlowStateException,
               UndestroyedSenderFlowsException,
               DuplicatedSenderStreamNameException {
        // --------------------------------------------------
        // 1. Check if the new Stream does not have any Flow.
        // --------------------------------------------------
        // Precondition: create a new Stream without any Flow
        String streamName = "OneFlowStream";
        BulkDataNTSenderStream stream
            = new BulkDataNTSenderStream(streamName, new BulkDataNTSenderStreamConfigurationDefault());

        // Evaluation: check the behavior of instance methods.
        assertEquals(streamName, stream.getName());
        assertEmptySenderStream(stream);

        // ----------------------
        // 2. Create a new Flow.
        // ----------------------
        // Precondition: Create a Stream with one Flow.
        String flowName = "TestFlow";
        List<String> flowNames
            = Arrays.asList(new String[]{ flowName });
        BulkDataNTSenderFlow flow = stream.createFlow(flowName, Optional.empty());

        // Evaluation: check the behavior of instance methods.
        assertEquals(streamName, stream.getName());
        assertSenderStreamWithFlowNames(stream, flowNames);
        assertEquals(flow, stream.getFlow(flowName));

        // --------------------
        // 3. Delete the Flow.
        // --------------------
        // Precondition: Delete the flow.
        stream.deleteFlow(flow);

        // Evaluation: check the behavior of instance methods.
        assertEquals(streamName, stream.getName());
        assertEmptySenderStream(stream);

        // Post process: delete the 
        stream.destroy();
    }

    @Test
    public void testMultipleFlow() {
        // TODO: test creating multiple flows, and delete them.
    }

    @Test
    public void testTooManyFlows() {
        // TODO: test creating too many flows and see what happens.
    }

    @Test
    public void testDeleteFlow() {
        // TODO: Test the behavior of deleteFlow().
        //       Specifically, test what happens if deleteFlow() is called
        //       for the Flow that does not exist.
    }

    @Test
    public void testDestroy() {
        // TODO: Test the behavior related to destroy().
        //       Specifically, test what happens after destroy() is called.
    }

    @Test
    public void testQosLibrary() {
        // TODO: test if QOS Library can be loaded correctly.
    }

    @Test
    public void testQosProfile() {
        // TODO: test if QOS Proflie can be loaded correctly.
    }

    @Test
    public void testDomainParticiapnt() {
        // TODO: test the domain participant in the Stream is created
        //       correctly.
    }

    @Test
    public void testConfiguration() {
        // TODO: test the various configuration.
    }

    /**
     * This assertion checks if the given Stream does not contain any Flow.
     *
     * @param stream The stream which is supposed to contain no Flow.
     */
    public void assertEmptySenderStream(BulkDataNTSenderStream stream) {
        String dummyFlowName = "DummyFlow";

        assertFalse(stream.existFlow(dummyFlowName));
        assertTrue(stream.getFlowNames().isEmpty());
        assertEquals(0, stream.getFlowNumber());

        try {
            stream.getFlow(dummyFlowName);
            fail();
        } catch (FlowNotExistException ex) {
            assertEquals(stream, ex.getStream());
            assertEquals(dummyFlowName, ex.getFlowName());
        }
    }

    /**
     * TODO: document this method
     */
    public void assertSenderStreamWithFlowNames(BulkDataNTSenderStream stream,
                                                List<String> flowNames) {
        // Test if all the given flow names exist.
        for (String flowName : flowNames) {
            assertTrue(stream.existFlow(flowName));
            assertTrue(stream.getFlowNames().contains(flowName));
        }
        assertEquals(flowNames.size(), stream.getFlowNames().size());
        assertEquals(flowNames.size(), stream.getFlowNumber());

        // Test the flow name that does not exist.
        String dummyFlowName = "DummyFlow";
        assertFalse(stream.existFlow(dummyFlowName));

        try {
            stream.getFlow(dummyFlowName);
            fail();
        } catch (FlowNotExistException ex) {
            assertEquals(stream, ex.getStream());
            assertEquals(dummyFlowName, ex.getFlowName());
        }
    }
}
