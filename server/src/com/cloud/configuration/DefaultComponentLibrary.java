/**
 *  Copyright (C) 2010 Cloud.com, Inc.  All rights reserved.
 * 
 * This software is licensed under the GNU General Public License v3 or later.
 * 
 * It is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package com.cloud.configuration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.cloud.agent.manager.AgentManagerImpl;
import com.cloud.agent.manager.allocator.HostAllocator;
import com.cloud.agent.manager.allocator.PodAllocator;
import com.cloud.agent.manager.allocator.impl.RecreateHostAllocator;
import com.cloud.agent.manager.allocator.impl.UserConcentratedAllocator;
import com.cloud.alert.AlertManagerImpl;
import com.cloud.alert.dao.AlertDaoImpl;
import com.cloud.async.AsyncJobExecutorContextImpl;
import com.cloud.async.AsyncJobManagerImpl;
import com.cloud.async.SyncQueueManagerImpl;
import com.cloud.async.dao.AsyncJobDaoImpl;
import com.cloud.async.dao.SyncQueueDaoImpl;
import com.cloud.async.dao.SyncQueueItemDaoImpl;
import com.cloud.capacity.dao.CapacityDaoImpl;
import com.cloud.certificate.dao.CertificateDaoImpl;
import com.cloud.cluster.DummyClusterManagerImpl;
import com.cloud.cluster.dao.ManagementServerHostDaoImpl;
import com.cloud.configuration.dao.ConfigurationDaoImpl;
import com.cloud.configuration.dao.ResourceCountDaoImpl;
import com.cloud.configuration.dao.ResourceLimitDaoImpl;
import com.cloud.consoleproxy.AgentBasedStandaloneConsoleProxyManager;
import com.cloud.consoleproxy.ConsoleProxyAllocator;
import com.cloud.consoleproxy.ConsoleProxyBalanceAllocator;
import com.cloud.dc.dao.AccountVlanMapDaoImpl;
import com.cloud.dc.dao.ClusterDaoImpl;
import com.cloud.dc.dao.DataCenterDaoImpl;
import com.cloud.dc.dao.DataCenterIpAddressDaoImpl;
import com.cloud.dc.dao.HostPodDaoImpl;
import com.cloud.dc.dao.PodVlanMapDaoImpl;
import com.cloud.dc.dao.VlanDaoImpl;
import com.cloud.deploy.SimplePlanner;
import com.cloud.domain.dao.DomainDaoImpl;
import com.cloud.event.dao.EventDaoImpl;
import com.cloud.ha.CheckOnAgentInvestigator;
import com.cloud.ha.HighAvailabilityManagerImpl;
import com.cloud.ha.InvestigatorImpl;
import com.cloud.ha.StorageFence;
import com.cloud.ha.XenServerInvestigator;
import com.cloud.ha.dao.HighAvailabilityDaoImpl;
import com.cloud.host.dao.DetailsDaoImpl;
import com.cloud.host.dao.HostDaoImpl;
import com.cloud.hypervisor.kvm.discoverer.KvmServerDiscoverer;
import com.cloud.hypervisor.xen.discoverer.XcpServerDiscoverer;
import com.cloud.maid.StackMaidManagerImpl;
import com.cloud.maid.dao.StackMaidDaoImpl;
import com.cloud.maint.UpgradeManagerImpl;
import com.cloud.maint.dao.AgentUpgradeDaoImpl;
import com.cloud.network.ExteralIpAddressAllocator;
import com.cloud.network.NetworkManagerImpl;
import com.cloud.network.configuration.ControlNetworkGuru;
import com.cloud.network.configuration.GuestNetworkGuru;
import com.cloud.network.configuration.PodBasedNetworkGuru;
import com.cloud.network.configuration.PublicNetworkGuru;
import com.cloud.network.dao.FirewallRulesDaoImpl;
import com.cloud.network.dao.IPAddressDaoImpl;
import com.cloud.network.dao.LoadBalancerDaoImpl;
import com.cloud.network.dao.LoadBalancerVMMapDaoImpl;
import com.cloud.network.dao.NetworkConfigurationDaoImpl;
import com.cloud.network.dao.NetworkRuleConfigDaoImpl;
import com.cloud.network.dao.RemoteAccessVpnDaoImpl;
import com.cloud.network.dao.VpnUserDaoImpl;
import com.cloud.network.router.DomainRouterManagerImpl;
import com.cloud.network.security.NetworkGroupManagerImpl;
import com.cloud.network.security.dao.IngressRuleDaoImpl;
import com.cloud.network.security.dao.NetworkGroupDaoImpl;
import com.cloud.network.security.dao.NetworkGroupRulesDaoImpl;
import com.cloud.network.security.dao.NetworkGroupVMMapDaoImpl;
import com.cloud.network.security.dao.NetworkGroupWorkDaoImpl;
import com.cloud.network.security.dao.VmRulesetLogDaoImpl;
import com.cloud.offerings.dao.NetworkOfferingDaoImpl;
import com.cloud.server.auth.MD5UserAuthenticator;
import com.cloud.service.dao.ServiceOfferingDaoImpl;
import com.cloud.storage.StorageManagerImpl;
import com.cloud.storage.allocator.FirstFitStoragePoolAllocator;
import com.cloud.storage.allocator.GarbageCollectingStoragePoolAllocator;
import com.cloud.storage.allocator.LocalStoragePoolAllocator;
import com.cloud.storage.allocator.StoragePoolAllocator;
import com.cloud.storage.dao.DiskOfferingDaoImpl;
import com.cloud.storage.dao.DiskTemplateDaoImpl;
import com.cloud.storage.dao.GuestOSCategoryDaoImpl;
import com.cloud.storage.dao.GuestOSDaoImpl;
import com.cloud.storage.dao.LaunchPermissionDaoImpl;
import com.cloud.storage.dao.SnapshotDaoImpl;
import com.cloud.storage.dao.SnapshotPolicyDaoImpl;
import com.cloud.storage.dao.SnapshotScheduleDaoImpl;
import com.cloud.storage.dao.StoragePoolDaoImpl;
import com.cloud.storage.dao.StoragePoolHostDaoImpl;
import com.cloud.storage.dao.UploadDaoImpl;
import com.cloud.storage.dao.VMTemplateDaoImpl;
import com.cloud.storage.dao.VMTemplateHostDaoImpl;
import com.cloud.storage.dao.VMTemplatePoolDaoImpl;
import com.cloud.storage.dao.VMTemplateZoneDaoImpl;
import com.cloud.storage.dao.VolumeDaoImpl;
import com.cloud.storage.download.DownloadMonitorImpl;
import com.cloud.storage.preallocatedlun.dao.PreallocatedLunDaoImpl;
import com.cloud.storage.secondary.SecondaryStorageDiscoverer;
import com.cloud.storage.secondary.SecondaryStorageManagerImpl;
import com.cloud.storage.secondary.SecondaryStorageVmDefaultAllocator;
import com.cloud.storage.snapshot.SnapshotManagerImpl;
import com.cloud.storage.snapshot.SnapshotSchedulerImpl;
import com.cloud.storage.upload.UploadMonitorImpl;
import com.cloud.template.TemplateManagerImpl;
import com.cloud.user.AccountManagerImpl;
import com.cloud.user.dao.AccountDaoImpl;
import com.cloud.user.dao.UserAccountDaoImpl;
import com.cloud.user.dao.UserDaoImpl;
import com.cloud.user.dao.UserStatisticsDaoImpl;
import com.cloud.utils.Pair;
import com.cloud.utils.component.Adapter;
import com.cloud.utils.component.ComponentLibrary;
import com.cloud.utils.component.ComponentLocator.ComponentInfo;
import com.cloud.utils.component.Manager;
import com.cloud.utils.db.GenericDao;
import com.cloud.vm.MauriceMoss;
import com.cloud.vm.UserVmManagerImpl;
import com.cloud.vm.dao.ConsoleProxyDaoImpl;
import com.cloud.vm.dao.DomainRouterDaoImpl;
import com.cloud.vm.dao.InstanceGroupDaoImpl;
import com.cloud.vm.dao.InstanceGroupVMMapDaoImpl;
import com.cloud.vm.dao.NicDaoImpl;
import com.cloud.vm.dao.SecondaryStorageVmDaoImpl;
import com.cloud.vm.dao.UserVmDaoImpl;
import com.cloud.vm.dao.VMInstanceDaoImpl;

public class DefaultComponentLibrary implements ComponentLibrary {

    protected final Map<String, ComponentInfo<GenericDao<?, ? extends Serializable>>> _daos = new LinkedHashMap<String, ComponentInfo<GenericDao<?, ?>>>();

    protected void addDao(String name, Class<? extends GenericDao<?, ? extends Serializable>> clazz) {
        addDao(name, clazz, null, true);
    }

    protected void addDao(String name, Class<? extends GenericDao<?, ? extends Serializable>> clazz, List<Pair<String, Object>> params,
            boolean singleton) {
        ComponentInfo<GenericDao<?, ? extends Serializable>> ComponentInfo = new ComponentInfo<GenericDao<?, ? extends Serializable>>(name, clazz, params, singleton);
        for (String key : ComponentInfo.getKeys()) {
            _daos.put(key, ComponentInfo);
        }
    }

    protected void addDaos() {
        addDao("StackMaidDao", StackMaidDaoImpl.class);
        addDao("VMTemplateZoneDao", VMTemplateZoneDaoImpl.class);
        addDao("DomainRouterDao", DomainRouterDaoImpl.class);
        addDao("HostDao", HostDaoImpl.class);
        addDao("VMInstanceDao", VMInstanceDaoImpl.class);
        addDao("UserVmDao", UserVmDaoImpl.class);
        addDao("ServiceOfferingDao", ServiceOfferingDaoImpl.class);
        addDao("DiskOfferingDao", DiskOfferingDaoImpl.class);
        addDao("DataCenterDao", DataCenterDaoImpl.class);
        addDao("HostPodDao", HostPodDaoImpl.class);
        addDao("IPAddressDao", IPAddressDaoImpl.class);
        addDao("VlanDao", VlanDaoImpl.class);
        addDao("PodVlanMapDao", PodVlanMapDaoImpl.class);
        addDao("AccountVlanMapDao", AccountVlanMapDaoImpl.class);
        addDao("VolumeDao", VolumeDaoImpl.class);
        addDao("EventDao", EventDaoImpl.class);
        addDao("UserDao", UserDaoImpl.class);
        addDao("UserStatisticsDao", UserStatisticsDaoImpl.class);
        addDao("DiskTemplateDao", DiskTemplateDaoImpl.class);
        addDao("FirewallRulesDao", FirewallRulesDaoImpl.class);
        addDao("LoadBalancerDao", LoadBalancerDaoImpl.class);
        addDao("NetworkRuleConfigDao", NetworkRuleConfigDaoImpl.class);
        addDao("LoadBalancerVMMapDao", LoadBalancerVMMapDaoImpl.class);
        addDao("DataCenterIpAddressDao", DataCenterIpAddressDaoImpl.class);
        addDao("NetworkGroupDao", NetworkGroupDaoImpl.class);
        addDao("IngressRuleDao", IngressRuleDaoImpl.class);
        addDao("NetworkGroupVMMapDao", NetworkGroupVMMapDaoImpl.class);
        addDao("NetworkGroupRulesDao", NetworkGroupRulesDaoImpl.class);
        addDao("NetworkGroupWorkDao", NetworkGroupWorkDaoImpl.class);
        addDao("VmRulesetLogDao", VmRulesetLogDaoImpl.class);
        addDao("AlertDao", AlertDaoImpl.class);
        addDao("CapacityDao", CapacityDaoImpl.class);
        addDao("DomainDao", DomainDaoImpl.class);
        addDao("AccountDao", AccountDaoImpl.class);
        addDao("ResourceLimitDao", ResourceLimitDaoImpl.class);
        addDao("ResourceCountDao", ResourceCountDaoImpl.class);
        addDao("UserAccountDao", UserAccountDaoImpl.class);
        addDao("VMTemplateHostDao", VMTemplateHostDaoImpl.class);
        addDao("UploadDao", UploadDaoImpl.class);
        addDao("VMTemplatePoolDao", VMTemplatePoolDaoImpl.class);
        addDao("LaunchPermissionDao", LaunchPermissionDaoImpl.class);
        addDao("ConfigurationDao", ConfigurationDaoImpl.class);
        addDao("VMTemplateDao", VMTemplateDaoImpl.class);
        addDao("HighAvailabilityDao", HighAvailabilityDaoImpl.class);
        addDao("ConsoleProxyDao", ConsoleProxyDaoImpl.class);
        addDao("SecondaryStorageVmDao", SecondaryStorageVmDaoImpl.class);
        addDao("ManagementServerHostDao", ManagementServerHostDaoImpl.class);
        addDao("AgentUpgradeDao", AgentUpgradeDaoImpl.class);
        addDao("SnapshotDao", SnapshotDaoImpl.class);
        addDao("AsyncJobDao", AsyncJobDaoImpl.class);
        addDao("SyncQueueDao", SyncQueueDaoImpl.class);
        addDao("SyncQueueItemDao", SyncQueueItemDaoImpl.class);
        addDao("GuestOSDao", GuestOSDaoImpl.class);
        addDao("GuestOSCategoryDao", GuestOSCategoryDaoImpl.class);
        addDao("StoragePoolDao", StoragePoolDaoImpl.class);
        addDao("StoragePoolHostDao", StoragePoolHostDaoImpl.class);
        addDao("DetailsDao", DetailsDaoImpl.class);
        addDao("SnapshotPolicyDao", SnapshotPolicyDaoImpl.class);
        addDao("SnapshotScheduleDao", SnapshotScheduleDaoImpl.class);
        addDao("PreallocatedLunDao", PreallocatedLunDaoImpl.class);
        addDao("ClusterDao", ClusterDaoImpl.class);
        addDao("CertificateDao", CertificateDaoImpl.class);
        addDao("NetworkConfigurationDao", NetworkConfigurationDaoImpl.class);
        addDao("NetworkOfferingDao", NetworkOfferingDaoImpl.class);
        addDao("NicDao", NicDaoImpl.class);
        addDao("InstanceGroupDao", InstanceGroupDaoImpl.class);
        addDao("InstanceGroupVMMapDao", InstanceGroupVMMapDaoImpl.class);
        addDao("RemoteAccessVpnDao", RemoteAccessVpnDaoImpl.class);
        addDao("VpnUserDao", VpnUserDaoImpl.class);
    }

    Map<String, ComponentInfo<Manager>> _managers = new HashMap<String, ComponentInfo<Manager>>();
    Map<String, List<ComponentInfo<Adapter>>> _adapters = new HashMap<String, List<ComponentInfo<Adapter>>>();

    @Override
    public synchronized Map<String, ComponentInfo<GenericDao<?, ?>>> getDaos() {
        if (_daos.size() == 0) {
            addDaos();
        }
        return _daos;
    }

    protected void addManager(String name, Class<? extends Manager> clazz, List<Pair<String, Object>> params, boolean singleton) {
        ComponentInfo<Manager> ComponentInfo = new ComponentInfo<Manager>(name, clazz, params, singleton);
        for (String key : ComponentInfo.getKeys()) {
            _managers.put(key, ComponentInfo);
        }
    }
    
    protected void addManager(String name, Class<? extends Manager> clazz) {
        addManager(name, clazz, null, true);
    }

    protected void addManagers() {
        addManager("StackMaidManager", StackMaidManagerImpl.class);
        addManager("agent manager", AgentManagerImpl.class);
        addManager("account manager", AccountManagerImpl.class);
        addManager("configuration manager", ConfigurationManagerImpl.class);
        addManager("network manager", NetworkManagerImpl.class);
        addManager("download manager", DownloadMonitorImpl.class);
        addManager("upload manager", UploadMonitorImpl.class);
        addManager("console proxy manager", AgentBasedStandaloneConsoleProxyManager.class);
        addManager("secondary storage vm manager", SecondaryStorageManagerImpl.class);
        addManager("vm manager", UserVmManagerImpl.class);
        addManager("upgrade manager", UpgradeManagerImpl.class);
        addManager("StorageManager", StorageManagerImpl.class);
        addManager("Cluster Manager", DummyClusterManagerImpl.class);
        addManager("SyncQueueManager", SyncQueueManagerImpl.class);
        addManager("AsyncJobManager", AsyncJobManagerImpl.class);
        addManager("AsyncJobExecutorContext", AsyncJobExecutorContextImpl.class);
        addManager("HA Manager", HighAvailabilityManagerImpl.class);
        addManager("Alert Manager", AlertManagerImpl.class);
        addManager("Template Manager", TemplateManagerImpl.class);
        addManager("Snapshot Manager", SnapshotManagerImpl.class);
        addManager("SnapshotScheduler", SnapshotSchedulerImpl.class);
        addManager("NetworkGroupManager", NetworkGroupManagerImpl.class);
        addManager("VmManager", MauriceMoss.class);
        addManager("DomainRouterManager", DomainRouterManagerImpl.class);
    }

    protected <T> void addAdapterChain(Class<T> interphace, List<Pair<String, Class<? extends T>>> adapters) {
        ArrayList<ComponentInfo<Adapter>> lst = new ArrayList<ComponentInfo<Adapter>>(adapters.size());
        for (Pair<String, Class<? extends T>> adapter : adapters) {
            @SuppressWarnings("unchecked")
            Class<? extends Adapter> clazz = (Class<? extends Adapter>)adapter.second();
            lst.add(new ComponentInfo<Adapter>(adapter.first(), clazz));
        }
        _adapters.put(interphace.getName(), lst);
    }
    
    @Override
    public synchronized Map<String, ComponentInfo<Manager>> getManagers() {
        if (_managers.size() == 0) {
            addManagers();
        }
        return _managers;
    }

    public void addAllAdapters() {
        
        List<Pair<String, Class<? extends HostAllocator>>> hostAllocators = new ArrayList<Pair<String, Class<? extends HostAllocator>>>();
        hostAllocators.add(new Pair<String, Class<? extends HostAllocator>>("FirstFitRouting", RecreateHostAllocator.class)); 
        //addAdapter("FirstFit", FirstFitAllocator.class);
        addAdapterChain(HostAllocator.class, hostAllocators);
        
        
        List<Pair<String, Class<? extends StoragePoolAllocator>>> poolAllocators = new ArrayList<Pair<String, Class<? extends StoragePoolAllocator>>>();
        poolAllocators.add(new Pair<String, Class<? extends StoragePoolAllocator>>("LocalStorage", LocalStoragePoolAllocator.class));
        poolAllocators.add(new Pair<String, Class<? extends StoragePoolAllocator>>("Storage", FirstFitStoragePoolAllocator.class));
        poolAllocators.add(new Pair<String, Class<? extends StoragePoolAllocator>>("GarbageCollecting", GarbageCollectingStoragePoolAllocator.class));
        addAdapterChain(StoragePoolAllocator.class, poolAllocators);
        
        List<Pair<String, Class<? extends PodAllocator>>> podAllocators = new ArrayList<Pair<String, Class<? extends PodAllocator>>>();
        podAllocators.add(new Pair<String, Class<? extends PodAllocator>>("User First", UserConcentratedAllocator.class));
        addAdapterChain(PodAllocator.class, podAllocators);
        
        List<Pair<String, Class<? extends ConsoleProxyAllocator>>> proxyAllocators = new ArrayList<Pair<String, Class<? extends ConsoleProxyAllocator>>>();
        proxyAllocators.add(new Pair<String, Class<? extends ConsoleProxyAllocator>>("Balance", ConsoleProxyBalanceAllocator.class));
        addAdapterChain(ConsoleProxyAllocator.class, proxyAllocators);
        
        // NetworkGuru
        addAdapterChain("GuestNetworkGuru", GuestNetworkGuru.class); 
        addAdapterChain("PublicNetworkGuru", PublicNetworkGuru.class); 
        addAdapterChain("PodBasedNetworkGuru", PodBasedNetworkGuru.class); 
        addAdapterChain("ControlNetworkGuru", ControlNetworkGuru.class);
        
        // Secondary Storage Vm Allocator
        addAdapterChain("Balance", SecondaryStorageVmDefaultAllocator.class); 

        // Ip Address Allocator
        addAdapterChain("Basic", ExteralIpAddressAllocator.class); 
    

        // User Authenticator
        addAdapterChain("MD5", MD5UserAuthenticator.class);
        
        // HA Investigator
        addAdapterChain("SimpleInvestigator", CheckOnAgentInvestigator.class); 
        addAdapterChain("XenServerInvestigator", XenServerInvestigator.class); 
        addAdapterChain("PingInvestigator", InvestigatorImpl.class);
        
        // HA Fence Builder
        addAdapterChain("StorageFenceBuilder", StorageFence.class);
        
        // Discoverer
        addAdapterChain("XCP Agent", XcpServerDiscoverer.class); 
        addAdapterChain("SecondaryStorage", SecondaryStorageDiscoverer.class); 
        addAdapterChain("KVM Agent", KvmServerDiscoverer.class);
        
        // Deployment Planner
        addAdapterChain("Simple", SimplePlanner.class); 
    }

    @Override
    public synchronized Map<String, List<ComponentInfo<Adapter>>> getAdapters() {
        if (_adapters.size() == 0) {
            addAdapters();
        }
        return _adapters;
    }
}
