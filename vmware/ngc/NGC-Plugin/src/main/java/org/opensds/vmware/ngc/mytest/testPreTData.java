package org.opensds.vmware.ngc.mytest;

import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.ServiceContent;
import org.opensds.vmware.ngc.common.Storage;
import org.opensds.vmware.ngc.common.StorageFactory;
import org.opensds.vmware.ngc.models.*;
import org.opensds.vmware.ngc.service.impl.DeviceServiceImpl;

import java.util.ArrayList;
import java.util.List;

import static org.opensds.vmware.ngc.common.StorageFactory.listStorages;


public class testPreTData {
    private static Storage storageMo;
    private static ServiceContent serviceContentMO;
    private static List<ManagedObjectReference> allDatacenter;


    public static Storage getDeviceMo() {
        return storageMo;
    }


    public static ServiceContent getServiceContentMO() {
        return serviceContentMO;
    }


    public static List<ManagedObjectReference> getAllDatacenter() {
        return allDatacenter;
    }


    public testPreTData() {

    }

    public static void testPre() {
        try {
            String[] list = listStorages();
            // device target;


            storageMo = StorageFactory.newStorage(list[0], "");

            storageMo.login("8.46.134.110",8088, "admin", "Admin@storage1");
            List<String> wwqnList = new ArrayList<>();
            ConnectMO connectMO = new ConnectMO(
                    "NGC_8.46.133.250_80477",
                    HOST_OS_TYPE.ESXI,
                    "iqn.1998-01.com.vmware:5c074f39-df97-67e8-a97e-384c4f66479f-5d7d7845",
                    wwqnList.toArray(new String[wwqnList.size()]),
                    ATTACH_MODE.RW,
                    ATTACH_PROTOCOL.ISCSI
            );
            long lnum = Long.valueOf(20l * 1024 *1024 * 1024).longValue();
            VolumeMO volumeMO = storageMo.createVolume("soda999",
                    ALLOC_TYPE.THIN,
                    lnum,
                    "0");
            storageMo.attachVolume(volumeMO.id, connectMO);


            // mo target
            allDatacenter = testGetDataCenter.getAllDatacenter();
            serviceContentMO = testVimContext.getServiceContent();

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }
}
