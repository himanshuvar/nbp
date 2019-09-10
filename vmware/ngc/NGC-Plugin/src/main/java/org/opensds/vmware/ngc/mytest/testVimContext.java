package org.opensds.vmware.ngc.mytest;


import com.vmware.vim25.*;
import com.vmware.vise.usersession.ServerInfo;
import org.opensds.vmware.ngc.base.VimFieldsConst;
import org.opensds.vmware.ngc.expections.InactiveSessionException;

import javax.net.ssl.*;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.soap.SOAPFaultException;
import java.util.*;


// 连接vimcontext demo
public class testVimContext {

    public static ManagedObjectReference _perfManager;
    public static ManagedObjectReference _propCollectorRef;
    public static ServiceContent serviceContent;
    public static VimPortType _vimPort = null;
    public static boolean isConnected = false;

    private static class TrustAllTrustManager implements TrustManager, X509TrustManager
    {
        public java.security.cert.X509Certificate[] getAcceptedIssuers()
        {
            return null;
        }

        public boolean isServerTrusted(java.security.cert.X509Certificate[] certs)
        {
            return true;
        }

        public boolean isClientTrusted(java.security.cert.X509Certificate[] certs)
        {
            return true;
        }

        public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) throws java.security.cert.CertificateException
        {
            return;
        }

        public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) throws java.security.cert.CertificateException
        {
            return;
        }
    }

    public static VimPortType get_vimPort() {
        return  _vimPort;
    }

    public static ServiceContent getServiceContent() {
        return serviceContent;
    }

    private static void trustAllHttpsCertificates() throws Exception {
        TrustManager[] trustAllCerts = new TrustManager[1];
        TrustManager tm = new TrustAllTrustManager();
        trustAllCerts[0] = tm;
        SSLContext sc = SSLContext.getInstance("SSL");
        SSLSessionContext sslsc = sc.getServerSessionContext();
        sslsc.setSessionTimeout(0);
        sc.init(null, trustAllCerts, null);
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
    }

    // 初始化 vimport
    static {
        try {
            HostnameVerifier hv = new HostnameVerifier() {
                public boolean verify(String urlHostName, SSLSession session) {
                    return true;
                }
            };
            trustAllHttpsCertificates();
            HttpsURLConnection.setDefaultHostnameVerifier(hv);

        }catch (Exception ex){
            ex.printStackTrace();
        }

        // test 是不是可以远程登录 vcenter
        try{

            VimService vimservice = new VimService();
            _vimPort = vimservice.getVimPort();
            Map<String, Object> ctxt = ((BindingProvider) _vimPort).getRequestContext();

            String url = null;
            ctxt.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, "https://8.46.135.64:443/sdk");
            ctxt.put(BindingProvider.SESSION_MAINTAIN_PROPERTY,
                    Boolean.TRUE);

            ManagedObjectReference morSvcInstance = new ManagedObjectReference();
            morSvcInstance.setType("ServiceInstance");
            morSvcInstance.setValue("ServiceInstance");
            serviceContent = _vimPort.retrieveServiceContent(morSvcInstance);
            _vimPort.login(serviceContent.getSessionManager(), "administrator@vsphere.local",
                    "Huawei@123", null);

            _perfManager = serviceContent.getPerfManager();
            _propCollectorRef = serviceContent.getPropertyCollector();

            System.out.println(serviceContent.getAbout().getFullName());
            System.out.println("Server type is " + serviceContent.getAbout().getApiType());
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    // 断开连接
    public static void diconnect() throws Exception {
        if (isConnected){
            _vimPort.logout(serviceContent.getSessionManager());
        }
        isConnected = false;
    }

    // 获取对象
    public static List<ObjectContent> retrievePropertiesALLObjects(List<PropertyFilterSpec>
                                                                           listpfs ) throws Exception{
        RetrieveOptions propObjectRetrieveOpts = new RetrieveOptions();
        List<ObjectContent> listobjcontent = new ArrayList<ObjectContent>();
        try{
            listobjcontent = _vimPort.retrieveProperties(_propCollectorRef, listpfs);
        }
        catch (SOAPFaultException sfe){
            sfe.printStackTrace();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return listobjcontent;
    }

    public static List<ManagedObjectReference> _getMoPropertyMangeObject(ManagedObjectReference moRef,
                                                                         String... properties)
            throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InactiveSessionException {

        List<ManagedObjectReference> retVal = new ArrayList<ManagedObjectReference>();
        if (moRef == null) {
            return null;
        }
        PropertyFilterSpec spec = new PropertyFilterSpec();
        spec.getPropSet().add(new PropertySpec());
        if ((properties == null || properties.length == 0)) {
            spec.getPropSet().get(0).setAll(Boolean.TRUE);
        } else {
            spec.getPropSet().get(0).setAll(Boolean.FALSE);
        }
        spec.getPropSet().get(0).setType(moRef.getType());
        spec.getPropSet().get(0).getPathSet().addAll(Arrays.asList(properties));
        spec.getObjectSet().add(new ObjectSpec());
        spec.getObjectSet().get(0).setObj(moRef);
        spec.getObjectSet().get(0).setSkip(Boolean.FALSE);
        List<PropertyFilterSpec> propertyFilterSpecList = new ArrayList<>(1);
        propertyFilterSpecList.add(spec);
        List<ObjectContent> objectContentList = _vimPort.retrieveProperties(_propCollectorRef,
                propertyFilterSpecList);

        if (objectContentList != null)
        {
            for (ObjectContent oc : objectContentList)
            {
                //根据object对象获得MOR对象
                List<DynamicProperty> dps = oc.getPropSet();
                if(dps != null){
                    for (DynamicProperty dp : dps) {
                        if(dp.getVal() instanceof ManagedObjectReference){
                            ManagedObjectReference mr = (ManagedObjectReference)dp.getVal();
                            retVal.add(mr);
                        }else if (dp.getVal() instanceof ArrayOfManagedObjectReference){
                            ArrayOfManagedObjectReference mrarr =
                                    (ArrayOfManagedObjectReference)dp.getVal();
                            retVal.addAll(mrarr.getManagedObjectReference());
                        }
                    }
                }
            }
        }
        return retVal;
    }

    public static Map<String, Object> _getMoPropertyContents(ManagedObjectReference moRef, String... properties)
            throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InactiveSessionException {
        if (moRef == null) {
            return null;
        }

        PropertyFilterSpec spec = new PropertyFilterSpec();
        spec.getPropSet().add(new PropertySpec());
        if ((properties == null || properties.length == 0)) {
            spec.getPropSet().get(0).setAll(Boolean.TRUE);
        } else {
            spec.getPropSet().get(0).setAll(Boolean.FALSE);
        }
        spec.getPropSet().get(0).setType(moRef.getType());
        spec.getPropSet().get(0).getPathSet().addAll(Arrays.asList(properties));
        spec.getObjectSet().add(new ObjectSpec());
        spec.getObjectSet().get(0).setObj(moRef);
        spec.getObjectSet().get(0).setSkip(Boolean.FALSE);
        List<PropertyFilterSpec> propertyFilterSpecList = new ArrayList<>(1);
        propertyFilterSpecList.add(spec);
        List<ObjectContent> objectContentList = _vimPort.retrieveProperties(_propCollectorRef,
                propertyFilterSpecList);
        return _getDynamicPropertiesFromObjectContents(objectContentList);
    }

    public static List<ObjectContent> _getMoPropertyContentsList(ManagedObjectReference moRef, String... properties)
            throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InactiveSessionException {
        if (moRef == null) {
            return null;
        }

        PropertyFilterSpec spec = new PropertyFilterSpec();
        spec.getPropSet().add(new PropertySpec());
        if ((properties == null || properties.length == 0)) {
            spec.getPropSet().get(0).setAll(Boolean.TRUE);
        } else {
            spec.getPropSet().get(0).setAll(Boolean.FALSE);
        }
        spec.getPropSet().get(0).setType(moRef.getType());
        spec.getPropSet().get(0).getPathSet().addAll(Arrays.asList(properties));
        spec.getObjectSet().add(new ObjectSpec());
        spec.getObjectSet().get(0).setObj(moRef);
        spec.getObjectSet().get(0).setSkip(Boolean.FALSE);
        List<PropertyFilterSpec> propertyFilterSpecList = new ArrayList<>(1);
        propertyFilterSpecList.add(spec);
        List<ObjectContent> objectContentList = _vimPort.retrieveProperties(_propCollectorRef,
                propertyFilterSpecList);
        return objectContentList;
    }


    public static  Map<String, Object> _getDynamicPropertiesFromObjectContents(List<ObjectContent> contentList) {
        if (contentList != null) {
            Map<String, Object> dpMap = new HashMap<>();
            ArrayList dpList = new ArrayList();

            for (ObjectContent content : contentList) {
                dpList.addAll(content.getPropSet());
            }
            for (Object dp : dpList) {
                DynamicProperty dynamicProperty = (DynamicProperty) dp;
                dpMap.put(dynamicProperty.getName(), dynamicProperty.getVal());
            }
            return dpMap;
        }
        return null;
    }


    public static HostConfigManager getHostConfigManager(ManagedObjectReference hostMo )
            throws InactiveSessionException, InvalidPropertyFaultMsg, RuntimeFaultFaultMsg
    {
        Map<String, Object> properties =
                _getMoProperties(hostMo, VimFieldsConst.PropertyNameConst.HostSystem.ConfigManager);
        return (HostConfigManager) properties.get(VimFieldsConst.PropertyNameConst.HostSystem.ConfigManager);
    }

    protected static Map<String, Object> _getMoProperties(ManagedObjectReference moRef, String... properties)
            throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InactiveSessionException {
        List<ObjectContent> objectContents = _getMoPropertyContentsList(moRef, properties);
        return _getDynamicPropertiesFromObjectContents(objectContents);
    }

    public TaskInfo createStorageTask(ManagedObjectReference hostMo, ServerInfo serverInfo, String taskId) {
        TaskInfo taskInfo = null;
        try {

            ManagedObjectReference taskManagerMo = serviceContent.getTaskManager();
            //taskInfo = _vimPort.createTask(taskManagerMo, hostMo, taskId, null, false, null);
            // comment out by jinke: createTask function add one more parameter in 6.5 sdk
            try
            {
                taskInfo = _vimPort.createTask(taskManagerMo, hostMo, taskId, null, false, null, null);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            _vimPort.setTaskState(taskInfo.getTask(), TaskInfoState.RUNNING, null, null);
        } catch (Throwable e) {

        }
        return taskInfo;
    }

    /*public static ResultInfo<List<StorageLun>> getHostLunListOfTargetByHostId(ManagedObjectReference hostMo,
                                                                               String targetType, String targetId, String targetIp) {
        //_logger.info(String.format("getHostLunListOfTargetByHostid start: %s %s", hostMo.getValue(), targetId));
        ResultInfo<List<StorageLun>> resultInfo = new ResultInfo<>();
        try {
            HostConfigInfo hostConfigInfo = _getHostConfigInfo(hostMo);
            List<StorageLun> hostLunListOfTarget = new ArrayList<>();
            Map<String, ScsiLun> scsiLunMap = new HashMap<>();
            for (ScsiLun scsiLun : hostConfigInfo.getStorageDevice().getScsiLun()) {
                scsiLunMap.put(scsiLun.getKey(), scsiLun);
            }
            Map<String, ScsiLun> scsiLunsBelongToTarget = new HashMap<>();

            Device targetOwningDevice = null;
            List<HostPlugStoreTopologyPath> pathsOfTheTarget;

            HostServiceImpl.PathsAndDeviceOfTarget pathsAndDeviceOfTarget = _getTargetsPathsAndDevice(hostConfigInfo, targetType, targetId);
            pathsOfTheTarget = pathsAndDeviceOfTarget.getPaths();
            targetOwningDevice = pathsAndDeviceOfTarget.getDevice();

            mark:
            for (HostMultipathInfoLogicalUnit logicalUnit : hostConfigInfo.getStorageDevice().getMultipathInfo().getLun()) {
                for (HostMultipathInfoPath multiPath : logicalUnit.getPath()) {
                    for (HostPlugStoreTopologyPath path : pathsOfTheTarget) {
                        if (multiPath.getName().equals(path.getName())) {
                            //_logger.debug("Lunkey = " + logicalUnit.getLun());
                            ScsiLun scsiLun = scsiLunMap.get(logicalUnit.getLun());
                            //_logger.debug("scsiLunName = " + scsiLun.getCanonicalName());
                            String wwn = getWWNFromLunCanonicalName(scsiLun.getCanonicalName().trim());
                            scsiLunsBelongToTarget.put(wwn, scsiLun);
                            continue mark;
                        }
                    }
                }
            }
            //for 6.0
            Map<String, List<String>> lunKeyToTopologyDevicePathMap = new HashMap<>();
            Map<String, String> pathKeyToTargetMap = new HashMap<>();
            Map<String, HostTargetTransport> targetTransportMap = new HashMap<>();
            for (HostPlugStoreTopologyDevice topologyDevice : hostConfigInfo.getStorageDevice().getPlugStoreTopology().getDevice()) {
                lunKeyToTopologyDevicePathMap.put(topologyDevice.getLun(), topologyDevice.getPath());
            }
            for (HostPlugStoreTopologyPath topologyPath : hostConfigInfo.getStorageDevice().getPlugStoreTopology().getPath()) {
                pathKeyToTargetMap.put(topologyPath.getKey(), topologyPath.getTarget());
            }
            for (HostPlugStoreTopologyTarget topologyTarget : hostConfigInfo.getStorageDevice().getPlugStoreTopology().getTarget()) {
                targetTransportMap.put(topologyTarget.getKey(), topologyTarget.getTransport());
            }
            for (String lunKey : scsiLunMap.keySet()) {
                ScsiLun lun = scsiLunMap.get(lunKey);
                //_logger.debug("lunKey = " + lunKey);
                String wwn = getWWNFromLunCanonicalName(lun.getCanonicalName().trim());
                if (lun instanceof HostScsiDisk && ((HostScsiDisk) lun).isLocalDisk()) {
                    continue;
                }
                List<String> pathList = lunKeyToTopologyDevicePathMap.get(lunKey);
                PathInfoLoop:
                for (String path : pathList) {
                    String targetKey = pathKeyToTargetMap.get(path);
                    HostTargetTransport transport = targetTransportMap.get(targetKey);
                    //_logger.debug("targetKey = " + targetKey);
                    *//*if (transport instanceof HostFibreChannelTargetTransport) {
                        String portWWPN = _getWWNFromFcLong(((HostFibreChannelTargetTransport) transport).getPortWorldWideName());
                        if (portWWPN.equals(targetId)) {
                            scsiLunsBelongToTarget.put(wwn, lun);
                        }
                    } else*//* if (transport instanceof HostInternetScsiTargetTransport) {
                        String iqn = ((HostInternetScsiTargetTransport) transport).getIScsiName();
                        //添加ip来过滤查询结果
                        List<String> ips = ((HostInternetScsiTargetTransport) transport).getAddress();
                        for (String temp : ips){
                            String ip = temp.split(":")[0];
                            if (iqn.equals(targetId) && ip.equals(targetIp)) {
                                scsiLunsBelongToTarget.put(wwn, lun);
                            }
                        }
                    } else {
                        //_logger.warn(String.format("Unsupportted transport type %s", transport));
                    }
//                  if(targetKey.contains(targetId)){
//                        scsiLunsBelongToTarget.put(wwn, lun);
//                  }
                }
            }

            //_logger.debug("scsiLunsBelongToTarget is " + scsiLunsBelongToTarget);
            for (ScsiLun scsiLun : scsiLunsBelongToTarget.values()) {

                StorageLun storageLun = new StorageLun();
                String wwn = getWWNFromLunCanonicalName(scsiLun.getCanonicalName().trim());
                //_logger.debug("wwn = " + wwn);
                if (targetOwningDevice != null) {
                    try {
                        storageLun = ((StorageDevice) targetOwningDevice).queryLunByWWN(wwn, null, null);
                    } catch (Throwable e) {
                        *//*_logger.debug(LogTraceUtil.getStackTrace(e));
                        _logger.debug("Can not find scsilun from device:" + scsiLun);
                        _logger.error(String.format("getHostIscsiLunListOfTargetByHostId query failed from %s for %s", targetOwningDevice, wwn));*//*
                    }
                }
                storageLun.updateWithScsiLun(scsiLun);
                if (!"".equals(storageLun.getWwn())) {
                    hostLunListOfTarget.add(storageLun);
                }
            }
            resultInfo.setData(hostLunListOfTarget);
            return resultInfo;
        } catch (Throwable e) {
            handleExceptions(resultInfo, e);
            return resultInfo;
        }
    }

    protected static HostConfigInfo _getHostConfigInfo(ManagedObjectReference hostMo)
            throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InactiveSessionException
    {
        HostConfigInfo hostConfigInfo;
        Map<String, Object> dpMap =
                _getMoProperties(hostMo, VimFieldsConst.PropertyNameConst.HostSystem.Config);
        hostConfigInfo = (HostConfigInfo) dpMap.get(VimFieldsConst.PropertyNameConst.HostSystem.Config);
        return hostConfigInfo;
    }

    private HostServiceImpl.PathsAndDeviceOfTarget _getTargetsPathsAndDevice(HostConfigInfo hostConfigInfo, String targetType, String targetId) {
        HostServiceImpl.PathsAndDeviceOfTarget result = new HostServiceImpl.PathsAndDeviceOfTarget();
        StorageDeviceRepository storageDeviceRepository = (StorageDeviceRepository) _deviceRepository;
        Map<String, Device> iscsiIpToDeviceMap = storageDeviceRepository.getIscsiIpToDeviceMap();
        //Map<String, Device> wwpnToDeviceMap = storageDeviceRepository.getScsiPortWWPNToDeviceMap();
        List<HostPlugStoreTopologyTarget> targets = hostConfigInfo.getStorageDevice().getPlugStoreTopology().getTarget();
        String targetKey;
        for (HostPlugStoreTopologyTarget target : targets) {
            targetKey = target.getKey();
            HostTargetTransport transport = target.getTransport();
            if (targetType.equals(HostHbaEnum.ISCSI.name())) {
                if (transport instanceof HostInternetScsiTargetTransport) {
                    HostInternetScsiTargetTransport iscsiTransport = (HostInternetScsiTargetTransport) transport;
                    if (iscsiTransport.getIScsiName().equals(targetId)) {
                        for (HostPlugStoreTopologyPath path : hostConfigInfo.getStorageDevice().getPlugStoreTopology().getPath()) {
                            if (path.getTarget().contains(targetKey)) {
                                result.getPaths().add(path);
                            }
                        }
                        if (iscsiTransport.getAddress().size() != 0) {
                            String ip = getIpFromIscsiAddress(iscsiTransport.getAddress().get(0));
                            //_logger.debug("The ip of the target's address is" + ip);
                            result.setDevice(iscsiIpToDeviceMap.get(ip));
                        } else {
                            //_logger.error(String.format("The ips for %s is empty.", targetId));
                        }
                        return result;
                    }
                }
            } *//*else if (targetType.equals(HostHbaEnum.FC.name()) || targetType.equals(HostHbaEnum.FCOE.name())) {
                if (transport instanceof HostFibreChannelTargetTransport) {
                    HostFibreChannelTargetTransport fcTransport = (HostFibreChannelTargetTransport) transport;
                    //String wwpn = _getWWNFromFcLong(fcTransport.getPortWorldWideName());
                    if (wwpn.equals(targetId)) {
                        result.setDevice(wwpnToDeviceMap.get(wwpn));
                    }
                    for (HostPlugStoreTopologyPath path : hostConfigInfo.getStorageDevice().getPlugStoreTopology().getPath()) {
                        if (path.getTarget().contains(targetKey)) {
                            result.getPaths().add(path);
                        }
                    }
                }
            }*//*
        }
        //_logger.debug("_getTargetsPathsAndDevice result:" + result);
        return result;
    }*/
}
