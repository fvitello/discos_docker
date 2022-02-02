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

import alma.acs.logging.AcsLogLevel;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.lang.Validate;

/**
 * This class represents one BulkData Stream at the sender side.
 * 
 * <p>
 * A Sender Stream is a conceptual group that contains one or more Flows.
 * Each stream can be identified by the name within one JVM process, and
 * the streams with the same name are now allowed in one JVM process.
 * 
 * <p>
 * To create a new Sender Stream, use the constructor
 * {@link #BulkDataNTSenderStream(String, BulkDataNTSenderStreamConfiguration)}
 * of this class.
 * 
 * <p>
 * After you create a Sender Stream, you can freely create new Flows
 * under the Stream by calling
 * {@link #createFlow(String, Optional)} or
 * {@link #createFlow(String, Optional, BulkDataNTSenderFlowConfiguration)}.
 * Once you finish using the Flow, destroy it by calling
 * {@link #deleteFlow(BulkDataNTSenderFlow)}. It will release resources
 * allocated to the Flow and unregister the Flow from this Stream.
 * 
 * <p>
 * When you finish using this Stream, call {@link #destroy()}. It will
 * first try to destroy all Flows in this Stream if any, and then destroy
 * the Stream itself and release resources allocated to this Stream.
 * {@link #destroy()} must be called before you finish your application.
 * Otherwise, your application may not properly quit.
 *
 * @author Takashi Nakamoto
 */
public class BulkDataNTSenderStream extends BulkDataNTStream {
    private boolean destroyed = false;
    private final List<BulkDataNTSenderFlow> flows;

    private final static Map<String, BulkDataNTSenderStream> senderStreams
        = new HashMap<>();

    /**
     * This constructor creates a new BulkData Sender Stream with
     * the given name and the given configuration.
     *
     * @param name Stream name that fulfills the criteria described in
     *             {@link #getName()}.
     * @param conf Configuration of the newly created Sender Stream.
     *
     * @exception DuplicatedSenderStreamNameException
     * if a Sender Stream with the given name already exists in the same
     * JVM process.
     * @exception InvalidStreamNameException
     * if the given name does not fulfill the criteria described in
     * {@link #getName()}
     * @exception QosXmlNotLoadedException
     * if QoS XML file has not been loaded yet. This could happen if your
     * application has not called either
     * {@link BulkDataNTGlobalConfiguration#loadQosXml()} or
     * {@link BulkDataNTGlobalConfiguration#loadQosXml(Path)} so far.
     * @throws QosLibraryNotExistException
     * if the library name specified by the configuration does not exist.
     * @throws QosProfileNotExistException
     * if the profile name specified by the configuration does not exist
     * @exception DdsException
     * if the underlying DDS library throws an exception.
     */
    public BulkDataNTSenderStream(String name,
                                  BulkDataNTSenderStreamConfiguration conf)
        throws DuplicatedSenderStreamNameException,
               InvalidStreamNameException,
               QosXmlNotLoadedException,
               QosLibraryNotExistException,
               QosProfileNotExistException,
               DdsException {
        super(checkDuplicationOfSenderStreamName(name), conf);

        this.flows = Collections.synchronizedList(new ArrayList<BulkDataNTSenderFlow>());

        logger.ifPresent(l -> l.log(AcsLogLevel.INFO,
                                    "Sender Stream: " + name +
                                    " has been created."));
    }

    /**
     * Create a new Flow in this Stream with the given name and the given
     * configuration.
     *
     * When the caller does not use the created Flow any more, it must
     * destroy the Flow by calling {@link #deleteFlow(BulkDataNTSenderFlow)}
     * or {@link #destroy()}.
     *
     * @param flowName The name of the new Flow.
     * @param listener The listener that monitors the events of the newly
     *                 created Sender Flow. This can be empty if the 
     *                 application does not need to listen to the events.
     * @param conf An instance that holds the configuration of the new Flow.
     *             The default configuration is used if null is provided, but
     *             it is rather recommended to call
     *             {@link #createFlow(String,Optional)}
     *             because it does not throw
     *             {@link IllegalConfigurationException}.
     *
     * @return The instance of the newly created Flow.
     *
     * @exception InvalidFlowNameException
     * if the given name does not fulfill the criteria described in
     * {@link BulkDataNTSenderFlow#getName()}
     * @throws DuplicatedFlowNameException
     * if a Flow with the same name already exists in this Stream.
     * @throws IllegalConfigurationException
     * if the given configuration contains illegal configuration value(s).
     * @throws StreamAlreadyDestroyedException
     * if this stream has been already destroyed.
     * @throws QosLibraryNotExistException
     * if the QoS library name given by the specified configuration does not
     * exist.
     * @throws QosProfileNotExistException
     * if the QoS profile name given by the specified configuration does not
     * exist in the QoS library.
     * @throws DdsException
     * if the underlying DDS library raises an exception or detects an error
     */
    public synchronized BulkDataNTSenderFlow
        createFlow(String flowName,
                   Optional<BulkDataNTSenderFlowListener> listener,
                   BulkDataNTSenderFlowConfiguration conf)
        throws InvalidFlowNameException,
               DuplicatedFlowNameException,
               IllegalConfigurationException,
               StreamAlreadyDestroyedException,
               QosLibraryNotExistException,
               QosProfileNotExistException,
               DdsException {
        Validate.notNull(listener);

        if (destroyed) {
            String msg
                = String.format("Cannot create Flow \"%s\". Stream \"%s\" " +
                                "has been already destroyed.",
                                flowName, getName());
            throw new StreamAlreadyDestroyedException(msg, this);
        }

        // Note: currently the limitation of the number of Flows in one
        //       Stream is not set. This means that Flows can be created
        //       as long as there is the sufficient resource.

        if (flows.stream().anyMatch(flow
                                    -> flow.getName().equals(flowName))) {
            String msg
                = "Flow \"" + flowName + "\" already exists in \""
                + getName() + "\" stream.";
            throw new DuplicatedFlowNameException(msg);
        }

        try {
            BulkDataNTSenderFlow flow
                = new BulkDataNTSenderFlow(flowName, this, listener, conf);
            flows.add(flow);

            return flow;
        } catch (StreamAlreadyDestroyedException ex) {
            // This must not happen, so wrap the exception with RuntimException.
            String msg = String.format("Stream \"%s\" was already destroyed " +
                                       "when creating a new Flow \"%s\".",
                                       getName(), flowName);
            throw new RuntimeException(msg, ex);
        }
    }

    /**
     * Create a new Flow in this Stream with the default configuration.
     *
     * When the caller does not use the created Flow any more, it must
     * destroy the Flow by calling {@link #deleteFlow(BulkDataNTSenderFlow)}
     * or {@link #destroy()}.
     *
     * @param flowName The name of the new Flow.
     * @param listener The listener that monitors the events of the newly
     *                 created Sender Flow. This can be empty if the 
     *                 application does not need to listen to the events.
     *
     * @return The instance of the newly created Flow.
     *
     * @exception InvalidFlowNameException
     * if the given name does not fulfill the criteria described in
     * {@link BulkDataNTSenderFlow#getName()}
     * @throws DuplicatedFlowNameException
     * if a Flow with the same name already exists in this Stream.
     * @throws StreamAlreadyDestroyedException
     * if this stream has been already destroyed.
     * @throws QosLibraryNotExistException
     * if the QoS library name given by the default configuration does not exist.
     * @throws QosProfileNotExistException
     * if the QoS profile name given by the default configuration does not exist
     * in the QoS library.
     * @throws DdsException
     * if the underlying DDS library raises an exception or detects an error
     *
     * @see #createFlow(String, Optional, BulkDataNTSenderFlowConfiguration)
     */
    public synchronized BulkDataNTSenderFlow
        createFlow(String flowName,
                   Optional<BulkDataNTSenderFlowListener> listener)
        throws InvalidFlowNameException,
               DuplicatedFlowNameException,
               StreamAlreadyDestroyedException,
               QosLibraryNotExistException,
               QosProfileNotExistException,
               DdsException {
        try {
            Validate.notNull(listener);
            
            return createFlow(flowName, listener, null);
        } catch (IllegalConfigurationException ex) {
            // This must never happen as long as default configuration is used.
            throw new RuntimeException("The default configuration is " +
                                       "considered to be illegal when " + 
                                       "creating a new Sender Flow.");
        }
    }

    /**
     * This method destroys the given Flow and removes it from this Stream.
     *
     * @param flow The instance of Flow to be deleted from this Stream.
     *
     * @throws FlowNotExistException
     * if the given Flow does not belong to this Stream.
     * @throws InappropriateSenderFlowStateException
     * if the Flow is not in {@link BulkDataNTSenderFlow.State#STOP} state.
     * @throws IllegalArgumentException
     * if the given Flow is null.
     * @throws DdsException
     * if the underlying DDS library fails to delete DDS entities. Even if
     * this occurs, this Flow is considered as destroyed. The application
     * can ignore this exception, but may need force quit at the end of
     * the application execution.
     */
    public synchronized void deleteFlow(BulkDataNTSenderFlow flow)
        throws FlowNotExistException,
               InappropriateSenderFlowStateException,
               DdsException {
        Validate.notNull(flow);

        if (flows.contains(flow)) {
            flow.destroy();
            flows.remove(flow);
        } else {
            throw new FlowNotExistException(this, flow.getName());
        }
    }

    /**
     * This method releases all the allocated resources to this Stream.
     * All Flows in this Stream will be destroyed by this method.
     *
     * <p>
     * This method must be called when this Stream is not needed any more.
     * Otherwise, the underlying RTI DDS library may prevent your program
     * from terminating.
     *
     * <p>
     * After calling this method, it is not possible to create a new Flow
     * in this Stream by calling {@link #createFlow(String,Optional)} or
     * {@link #createFlow(String,Optional,BulkDataNTSenderFlowConfiguration)}.
     * Other method calls will behave as if this Stream does not have
     * any Flow. For example, {@link #deleteFlow(BulkDataNTSenderFlow)}
     * will throw {@link FlowNotExistException}.
     *
     * <p>
     * If this Stream has been already destroyed, this method does nothing.
     * 
     * <p>
     * If your application manages the lifecycle of the Flows in this Stream,
     * it is recommended that your application first deletes all Flows by
     * calling {@link #deleteFlow(BulkDataNTSenderFlow)}, and then call this
     * method to destroy this Stream. This method first tries to delete all
     * the Flows in it in a normal way, but, if it fails, this Stream tries
     * to forcibly destroy all Flows, which may result in unexpected
     * conclusion. It is not guaranteed that the BD_STOP frame is sent to
     * the Receivers. It is also not guaranteed that the resources allocated
     * for the Flows are released. In some cases, some DDS entities may
     * keep alive and prevent your application from quitting in a normal way.
     * 
     * <p>
     * Another thread can interrupt the thread executing this method. In
     * that case, this Stream tries to release all resources as much as
     * possible, but some Flows may be left undestroyed, which may prevent
     * your application from quitting in a normal way.
     * 
     * <p>
     * At most, this method may be blocked for up to approximately the
     * maximum ACK and send frame timeout of all Sender Flow configurations
     * plus one second.
     *
     * @exception DdsException
     * if the underlying DDS library throws an exception. When this happens,
     * this Stream is left as undestroyed. Your application can call destroy()
     * method again, or ignore this exception. If this Stream is left as
     * undestroyed, but maybe your application cannot be terminated properly.
     * @throws UndestroyedSenderFlowsException
     * if some Flows couldn't be destroyed for some reason. Even if some Flows
     * couldn't be destroyed, this Stream is considered as destroyed. 
     */
    @Override
    public synchronized void destroy()
        throws DdsException,
               UndestroyedSenderFlowsException {
        if (destroyed) {
            logger.ifPresent(l -> l.log(AcsLogLevel.DEBUG,
                                        "Sender Stream: " + getName() +
                                        " was already destroyed."));
            return;
        }
        
        // This map collects the undestroyed Flows and the reason
        // why it couldn't be destroyed. This map will be accessed by
        // several threads, so ConcurrentHashMap is used here.
        Map<BulkDataNTSenderFlow, Exception> reasons
            = new ConcurrentHashMap<>();

        // Destroy all flows in this stream.
        //
        // First, let's try to destroy all flows. For the Flows for
        // which destroy() fails, try forceDestroy() later.
        BulkDataNTSenderFlow[] tmpFlows = flows.toArray(new BulkDataNTSenderFlow[0]);
        for (int i = 0; i < tmpFlows.length; i++) {
            BulkDataNTSenderFlow flow = tmpFlows[i];
            try {
                flow.destroy();
                flows.remove(flow);
            } catch (InappropriateSenderFlowStateException ex) {
                reasons.put(flow, ex);
            } catch (DdsException ex) {
                reasons.put(flow, ex);
            }
        }

        if (!flows.isEmpty()) {
            // If there are one or more Flows that couldn't be destroyed
            // in a normal way, try to forcibly destroy them.
            
            ExecutorService executor
                = Executors.newFixedThreadPool(flows.size());
            List<Callable<Void>> destroyThreads = new ArrayList<>();

            Duration maxTimeout = maxTimeout(flows);

            tmpFlows = flows.toArray(new BulkDataNTSenderFlow[0]);
            for (int i = 0; i < tmpFlows.length; i++) {
                BulkDataNTSenderFlow undestroyedFlow = tmpFlows[i];
                destroyThreads.add(new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        try {
                            undestroyedFlow.forceDestroy();
                        } catch (DdsException ex) {
                            // Even if this exception happens,
                            // it can be considered as destroyed.
                            // See javadoc of forceDestroy().
                            flows.remove(undestroyedFlow);
                        } catch (Exception ex) {
                            reasons.put(undestroyedFlow, ex);
                        }
                        return null;
                    }
                });
            }
            
            // Wait until forceDestroy() method calls of all the Flows
            // return, or until the timeout is elapsed. forceDestroy()
            // is supposed to be blocked for up to the ACK timeout
            // or the send frame timeout, whichever is longer. Thus,
            // the timeout for forceDestroy() is set to the maximum of
            // the ACK timeout and the send frame timeout settings of
            // all the undestroyed Flows, plus some margin.
            assert maxTimeout.getSeconds() <= Integer.MAX_VALUE;
            long maxTimeoutNanos = maxTimeout.getSeconds() * 1000000000L
                                 + maxTimeout.getNano();
            maxTimeoutNanos += 1000000000; // Add one second overhead.

            try {
                executor.invokeAll(destroyThreads,
                                   maxTimeoutNanos, TimeUnit.NANOSECONDS);
                executor.shutdown();
            } catch (InterruptedException ex) {
                // If this is interrupted, then let's give up destroying
                // the Flows.
                
                // This method triggers Thread.interrupt() of all
                // destroyThreads, and it is expected that 
                // InterruptedException is set to "reasons" variable.
                executor.shutdownNow();
            }
        }

        super.destroy();
        destroyed = true;
        
        logger.ifPresent(l -> l.log(AcsLogLevel.INFO,
                                    "Sender Stream: " + getName()
                                    + " has been destroyed."));

        Map<BulkDataNTSenderFlow, Exception> undestroyedFlowsAndReasons
        = new HashMap<>();
        reasons.forEach((flow, reason) -> {
            if (!(flow.isDestroyed())) {
                undestroyedFlowsAndReasons.put(flow, reason);
            }
        });
        
        if (!(undestroyedFlowsAndReasons.isEmpty())) {
            String msg = "Failed to destroy some Sender Flows.";
            throw new UndestroyedSenderFlowsException(msg,
                                                      undestroyedFlowsAndReasons);
        }
    }

    /**
     * @deprecated This method is here because C++ implementation has an equivalent
     * method. However, the use case of this method is unclear and currently is
     * not implemented. This method will be removed unless there is an explicit
     * request.
     *
     * @param config not supported
     */
    public void createMultipleFlowsFromConfig(String config) {
        throw new UnsupportedOperationException();
    }

    /**
     * This method returns the instance of the Flow with the given name
     * in this Stream.
     *
     * @param flowName The name of the Flow.
     *
     * @return The instance of the Flow if present in this Stream.
     *
     * @throws FlowNotExistException
     * if the Flow with the given name does not exist in this Stream.
     */
    public synchronized BulkDataNTSenderFlow getFlow(String flowName)
        throws FlowNotExistException {
        for (BulkDataNTSenderFlow flow : flows) {
            if (flow.getName().equals(flowName)) {
                return flow;
            }
        }

        throw new FlowNotExistException(this, flowName);
    }

    /**
     * This method checks if a Flow with the given name exists in this Stream.
     *
     * @param flowName The name of the Flow.
     *
     * @return True if the Flow with the given name exists in this Stream.
     *         Otherwise, false.
     */
    public synchronized boolean existFlow(String flowName) {
        for (BulkDataNTSenderFlow flow : flows) {
            if (flow.getName().equals(flowName)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns the names of all the Flows in this Stream.
     *
     * @return Set of Flow names.
     */
    public synchronized Set<String> getFlowNames() {
        return flows.stream()
            .map(flow -> flow.getName())
            .collect(Collectors.toSet());
    }

    /**
     * Returns the number of Flows in this Stream.
     *
     * @return The number of Flows in this Stream.
     */
    public synchronized int getFlowNumber() {
        return flows.size();
    }

    /**
     * This method checks if the Sender Stream with the given name already
     * exists in the current JVM process. If it does, this method throws
     * {@link DuplicatedSenderStreamException}. If not, this method
     * returns the same name.
     *
     * @param name The Stream name to check the existence.
     *
     * @return The same Stream name as specified by the argument.
     */
    private static String checkDuplicationOfSenderStreamName(String name)
        throws DuplicatedSenderStreamNameException {
        synchronized (senderStreams) {
            BulkDataNTSenderStream stream
                = senderStreams.get(name);
            if (stream != null) {
                throw new DuplicatedSenderStreamNameException(stream);
            }
            return name;
        }
    }
    
    /**
     * This method returns the maximum timeout setting (including
     * both ACK timeout and send frame timeout) of the given Sender Flows.
     * 
     * <p>
     * The returned duration is in the range between 0 and
     * {@value java.lang.Integer#MAX_VALUE}.999999999 seconds. 
     * 
     * @param flows The list of Sender Flows.
     * 
     * @return The maximum timeout in the range between 0 and 
     *         {@value java.lang.Integer#MAX_VALUE}.999999999 seconds. 
     */
    private static Duration maxTimeout(List<BulkDataNTSenderFlow> flows) {
        Duration max = Duration.ofSeconds(0);
        
        for (BulkDataNTSenderFlow flow: flows) {
            BulkDataNTSenderFlowConfiguration conf = flow.getConfiguration();
            Duration ackTimeout = conf.getAckTimeout();
            assert !(ackTimeout.isNegative());
            assert ackTimeout.getSeconds() <= Integer.MAX_VALUE;
            assert ackTimeout.getNano() < 1000000000;
            if (ackTimeout.compareTo(max) > 0) {
                max = ackTimeout;
            }
            
            Duration sendFrameTimeout = conf.getSendFrameTimeout();
            assert !(sendFrameTimeout.isNegative());
            assert sendFrameTimeout.getSeconds() <= Integer.MAX_VALUE;
            assert sendFrameTimeout.getNano() < 1000000000;
            if (sendFrameTimeout.compareTo(max) > 0) {
                max = sendFrameTimeout;
            }
        }
        
        return max;
    }
}
