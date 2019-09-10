package org.opensds.vmware.ngc.mytest;

import com.google.gson.Gson;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vise.usersession.ServerInfo;
import org.opensds.vmware.ngc.common.Storage;
import org.opensds.vmware.ngc.common.StorageFactory;
import org.opensds.vmware.ngc.model.DatastoreInfoOld;
import org.opensds.vmware.ngc.model.VolumeInfo;
import org.opensds.vmware.ngc.model.datastore.VMFSDatastore;
import org.opensds.vmware.ngc.models.ALLOC_TYPE;
import org.opensds.vmware.ngc.models.VolumeMO;
import org.opensds.vmware.ngc.service.HostService;
import org.opensds.vmware.ngc.service.impl.DatastoreServiceImpl;
import org.opensds.vmware.ngc.model.datastore.Datastore;
import org.opensds.vmware.ngc.service.impl.HostServiceImpl;
import org.opensds.vmware.ngc.util.CapacityUtil;
import org.opensds.vmware.ngc.util.FilterUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.*;

import static org.opensds.vmware.ngc.common.StorageFactory.listStorages;
import static org.opensds.vmware.ngc.controller.DatastoreController.convertToDatastore;


public class testcx {

    private Storage storageMo;

    public Storage getDeviceMo() {
        return storageMo;
    }

    public void setDeviceMo(Storage deviceMo) {
        this.storageMo = storageMo;
    }

    private static Storage device;

    public static enum Errorcode {

        VOLUME_NOT_EXIST(1212);

        private long value;

        private Errorcode(long value) {
            this.value = value;
        }

        public long getValue() {
            return value;
        }


    }

    public static void connectDevice() {
        try {
            String[] list = listStorages();
            device = StorageFactory.newStorage(list[0], "");
            device.login("8.44.168.30", 8088, "admin", "Admin@storage1");

        } catch (Exception ex) {
            System.out.println("error" + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        try {
            connectDevice();
            Date d = new Date();
            String s = null;
            System.out.println(d.toString() + " : Print test!");
            List<VolumeMO> volumeMOs = device.listVolumes();
            d = new Date();
            System.out.println(d.toString() + " : Print test fininshed!");

            String strJson = "{\"name\":\"res090701\",\"type\":\"vmfsDatastore\",\"isCreateDatastore\":true," +
                    "\"vmfsVersion\":\"VMFS5\",\"volumeInfos\":[{\"name\":\"res090701\",\"description\":\"\"," +
                    "\"capacity\":\"3GB\",\"type\":\"thin\",\"storageType\":\"OceanStor\"," +
                    "\"storageId\":\"urn:vri:opensds:Storage:8.44.168.30\",\"storagePoolId\":\"2\"}]}";

            Datastore datastore = convertToDatastore(strJson);
            VMFSDatastore vmfsDatastore = ((VMFSDatastore) datastore);
            String name = vmfsDatastore.getName();
            String version = vmfsDatastore.getVmfsVersion().substring(4);
            VolumeInfo volumeInfo = ((VMFSDatastore) datastore).getVolumeInfos()[0];
            VolumeMO volumeMO = device.createVolume(volumeInfo.getName(),
                    volumeInfo.getAllocType().equals("thin") ? ALLOC_TYPE.THIN : ALLOC_TYPE.THICK,
                    CapacityUtil.convertCapToLong(volumeInfo.getCapacity()),
                    volumeInfo.getStoragePoolId());

            connectDevice();

            test2();
            test3();
        } catch (Exception ex) {
            System.out.println("error" + ex.getMessage());
        }
/*        try {
            VolumeInfo volumeInfo = new VolumeInfo();
            volumeInfo.setName("abc");
            Field field = volumeInfo.getClass().getDeclaredField("name");
            field.setAccessible(true);
            Object object = field.get(volumeInfo);
            if (object.equals("abc")) {
                System.out.println("yes!");
            } else {
                System.out.println("no!");
            }
        }catch (Exception ex) {
            System.out.println(ex.getMessage());
        }*/
        /*try {
            ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(2, 64,
                    60L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

            Future<Integer> future = threadPoolExecutor.submit((Callable) () -> {
                System.out.println("Thread ID:" + Thread.currentThread().getId());
                Thread.sleep(10000);
                return 100;
            });

            System.out.println("Before get the num!! ");

            Integer te = future.get();

            System.out.println("After get the num!! ");

        } catch (Exception ex) {

        }

        HashMap<String, String> MODEL_MAP = new HashMap<>();

        MODEL_MAP.put("128", "6810 V5");

        if (!MODEL_MAP.containsKey("model")) {
            System.out.println("yes");
        }

        if (MODEL_MAP.containsKey("")) {
            System.out.println("yes");
        }

        Object object = Optional.ofNullable(null);

        if (object == null) {
            System.out.println("null");
        }

        Datastore dsInfo = null;
        try {
            String strjson = "{\"datastoreName\":\"ds1\",\"datastoreType\":\"vmfsDatastore\",
            \"isCreateDatastore\":true,\"volumeInfos\":[{\"volumeName\":\"ds1\",
            \"volumeDescription\":\"lunDescription\",\"storagePoolId\":\"0\",\"storageType\":\"oceanstor\",
            \"storageID\":\"20190505\",\"VolumeCapacity\":\"1024\",\"volumeType\":\"thin\"}]}";
            Gson gson = new Gson();
            dsInfo = gson.fromJson(strjson, Datastore.class);
        } catch (Exception ex) {
            System.out.print(ex.getMessage());
        }

        DatastoreInfoOld datastore = new DatastoreInfoOld();
        datastore.setDatastoreName("testNGC_cx0011");
        String vmfsDatastore = "vmfsDatastore";
        datastore.setDatastoreType(vmfsDatastore);
        datastore.setLunCapacity(3);
        datastore.setFileVersion("VMFS5");
        datastore.setAllocType("thin");
        datastore.setStoragePoolId("0");
        datastore.setIsCreateDatastore(true);*/

        try {
            List<ManagedObjectReference> allDatacenter = testGetDataCenter.getAllDatacenter();
            ManagedObjectReference hostMo = new ManagedObjectReference();

            for (ManagedObjectReference mo : allDatacenter) {
                List<ManagedObjectReference> objListhostfolder = testVimContext._getMoPropertyMangeObject(mo,
                        "hostFolder");
                for (ManagedObjectReference hostfold : objListhostfolder) {
                    List<ManagedObjectReference> ManagedEntity = testVimContext._getMoPropertyMangeObject(hostfold,
                            "childEntity");
                    for (ManagedObjectReference mahostcluster : ManagedEntity) {
                        List<ManagedObjectReference> Managedhost = testVimContext._getMoPropertyMangeObject
                                (mahostcluster, "host");
                        for (ManagedObjectReference mahostli : Managedhost) {
                            String str = mahostli.getValue();
                            if (mahostli.getValue().equalsIgnoreCase("host-103")) {
                                hostMo = mahostli;
                                break;
                            }
                        }
                    }
                }
            }
            // test create datastore
            /*List<ManagedObjectReference> array = new ArrayList<>();
            array.add(hostMo);
            ManagedObjectReference[] list = new ManagedObjectReference[1];
            list[0] = hostMo;
            DatastoreServiceImpl DatastoreServiceImpl = new DatastoreServiceImpl();
            DatastoreServiceImpl.createVmfsDatastore(list, new ServerInfo(), datastore, testPreTData.getDeviceMo());*/

            // test create query datastoreinfo
            HostService hostServiceImpl = HostServiceImpl.getInstance();

            hostServiceImpl.getDatastoreList(hostMo, new ServerInfo());

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    static class person {

        public String name;

        public person(String name) {
            this.name = name;
        }
    }


    public static void test2() {
        try {
            VolumeMO volumeMO = device.queryVolumeByID("630d17e100b30207180462380000006b");
            VolumeMO.StatusE status = volumeMO.status;
            String str = status.toString();
            Optional<VolumeMO> opt = device.listVolumes().stream().filter(n -> (
                    n.wwn.equals("648435a100588052148bda870000027f")
            )).findFirst();
            if (opt.isPresent()) {
                System.out.println("find the wwn");
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

    }

    public static void test3() throws Exception {
        List<VolumeMO> volumeMOList = FilterUtils.filterList(device.listVolumes(), "NAME", "");
        Map<String, VolumeInfo> allVolumeInfo = new ConcurrentHashMap<>();
        volumeMOList.forEach(n -> {
            VolumeInfo volumeInfo = new VolumeInfo();
            allVolumeInfo.put(n.wwn, volumeInfo.convertVolumeMO2Info(n));
        });
    }

    public static void test1() {
        List<person> stringList = new ArrayList<>();
        List<person> orgin = new ArrayList<>();

        orgin.add(new person("xiao wang"));
        orgin.add(new person("xiao li"));

        orgin.forEach(n -> {
            System.out.println("old list " + n.name);
        });

        stringList = (ArrayList<person>) ((ArrayList<person>) orgin).clone();
        stringList.forEach(n -> {
            n.name = "";
        });

        orgin.forEach(n -> {
            System.out.println("new list" + n.name);
        });
    }
}

