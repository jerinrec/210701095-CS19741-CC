package org.cloudbus.cloudsim.examples;

import java.text.DecimalFormat;
import java.util.*;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.*;

public class CloudSimExample1 {
	private static List<Cloudlet> cloudletList;
	private static List<Vm> vmlist;
	static int mips = 1000;
	static String vmm = "Xen";

	public static void main(String[] args) {
		Log.println("Starting CloudSimExample1...");
		try {
			CloudSim.init(1, Calendar.getInstance(), false);

			createDatacenter("Datacenter_0");

			DatacenterBroker broker = new DatacenterBroker("Broker");
			int brokerId = broker.getId();

			vmlist = new ArrayList<>();

			int vmid = 0, ram = 512, pesNumber = 1;
			long size = 10000, bw = 1000;

			vmlist.add(new Vm(vmid, brokerId, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared()));

			broker.submitGuestList(vmlist);

			cloudletList = new ArrayList<>();

			int id = 0;
			long length = 400000, fileSize = 300, outputSize = 300;
			UtilizationModel utilizationModel = new UtilizationModelFull();

			Cloudlet cloudlet = new Cloudlet(id, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
			cloudlet.setUserId(brokerId);
			cloudlet.setGuestId(vmid);

			cloudletList.add(cloudlet);

			broker.submitCloudletList(cloudletList);

			CloudSim.startSimulation();
			CloudSim.stopSimulation();

			printCloudletList(broker.getCloudletReceivedList());

			Log.println("CloudSimExample1 finished!");
		} catch (Exception e) {}

	}

	private static Datacenter createDatacenter(String name) {
		List<Host> hostList = new ArrayList<>();
		List<Pe> peList = new ArrayList<>();

		peList.add(new Pe(0, new PeProvisionerSimple(mips)));
		int hostId = 0, ram = 2040, bw = 10000;
		long storage = 1000000;

		hostList.add(new Host(hostId, new RamProvisionerSimple(ram), new BwProvisionerSimple(bw), storage, peList, new VmSchedulerTimeShared(peList)));

		String arch = "x86", os = "Linux";
		double time_zone = 10.0, cost = 3.0, costPerMem = 0.05, costPerStorage = 0.001, costPerBw = 0;
		LinkedList<Storage> storageList = new LinkedList<>();
		DatacenterCharacteristics characteristics = new DatacenterCharacteristics(arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw);

		Datacenter datacenter = null;
		try {
			datacenter = new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), storageList, 0);
		} catch (Exception e) {}

		return datacenter;
	}


	private static void printCloudletList(List<Cloudlet> list) {
		String indent = "        ";
		Log.println("\n========== OUTPUT ==========");
		Log.println("Cloudlet ID" + indent + "STATUS" + indent + "Data center ID" + indent + "VM ID" + indent + "Time" + indent + "Start Time" + indent + "Finish Time");
		DecimalFormat dft = new DecimalFormat("###.##");
		for (Cloudlet cloudlet : list) {
			Log.print(cloudlet.getCloudletId() + indent);
			if (cloudlet.getStatus() == Cloudlet.CloudletStatus.SUCCESS) {
				Log.print("SUCCESS");
				Log.println(indent + cloudlet.getResourceId()
						+ indent + cloudlet.getGuestId() 
						+ indent + dft.format(cloudlet.getActualCPUTime()) 
						+ indent + dft.format(cloudlet.getExecStartTime())
						+ indent + dft.format(cloudlet.getExecFinishTime()));
			}
		}
	}
}