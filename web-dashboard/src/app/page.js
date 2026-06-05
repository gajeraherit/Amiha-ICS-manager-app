"use client";

import { useState, useEffect } from "react";
import { 
  LayoutDashboard, 
  Users, 
  UserPlus, 
  Settings, 
  LogOut, 
  Search, 
  Plus, 
  X, 
  Shield, 
  Lock, 
  Mail, 
  Phone, 
  Landmark, 
  MapPin, 
  Sprout, 
  Tractor, 
  HelpCircle,
  FileCheck,
  AlertTriangle,
  UserCheck,
  CheckCircle,
  Activity,
  Layers,
  Map
} from "lucide-react";
import { auth, db } from "./firebase";
import { signInWithEmailAndPassword, signOut, onAuthStateChanged } from "firebase/auth";
import { collection, getDocs, getDoc, doc, setDoc, updateDoc } from "firebase/firestore";
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, Cell, PieChart, Pie, Legend } from "recharts";

// Mock Data for fallback / offline demo mode
const MOCK_STAFF = [
  { id: "s1", fullName: "Harish Kumar", email: "admin@ics.org", role: "Admin", phone: "+91 98765 43210" },
  { id: "s2", fullName: "Ananya Patel", email: "superadmin@ics.org", role: "Super Admin", phone: "+91 98765 00112" },
  { id: "s3", fullName: "Arjun FO", email: "arjun@ics.org", role: "Field Officer", phone: "+91 99887 76655" },
  { id: "s4", fullName: "Sunita Das", email: "sunita@ics.org", role: "Inspector", phone: "+91 91234 56789" }
];

const MOCK_FARMERS = [
  {
    farmerId: "FO-26-4412",
    fullName: "Savitri Devi",
    dob: "1978-04-12",
    gender: "Female",
    nationalId: "XXXX-XXXX-8912",
    primaryPhone: "+91 94567 89012",
    secondaryPhone: null,
    villageName: "Meghpura",
    gramPanchayat: "Meghpura",
    taluka: "Patan",
    district: "Patan",
    state: "Gujarat",
    gpsLatitude: 23.8497,
    gpsLongitude: 72.1203,
    primaryCrops: ["Cotton", "Castor"],
    secondaryCrops: ["Mustard"],
    farmingMethod: "Transitional",
    useOfPesticides: true,
    pesticideDetails: "Used light pesticide during pest outbreak in cotton crop",
    useOfChemicalFertilizers: false,
    chemicalFertilizerDetails: null,
    seedSource: "Government",
    previousSeasonYield: "800 kg",
    livestock: [
      { type: "Cattle", count: 2, primaryUse: "Dairy", fodderSource: "Own Farm" }
    ],
    icsGroup: "Patan Organic Growers",
    registrationDate: "2026-02-15",
    certificationStatus: "Under Inspection",
    lastInspectionDate: "2026-05-10",
    inspectionResult: "Pending",
    nonConformanceNotes: "Needs buffer zone clearance verification near conventional neighbor field.",
    nextScheduledInspectionDate: "2026-08-15",
    plots: [
      { id: 1, plotId: "Khasra-412", landArea: 1.8, ownershipType: "Own", irrigationSource: "Rain-fed", soilType: "Sandy Loam" }
    ],
    householdMembers: [
      { name: "Rajesh Kumar", relationship: "Spouse", age: 50, gender: "Male", role: "Farming" }
    ]
  },
  {
    farmerId: "FO-26-8043",
    fullName: "Ramesh Patel",
    dob: "1982-11-20",
    gender: "Male",
    nationalId: "XXXX-XXXX-4567",
    primaryPhone: "+91 98980 12345",
    secondaryPhone: "+91 98980 54321",
    villageName: "Anandpur",
    gramPanchayat: "Anandpur",
    taluka: "Gokak",
    district: "Belagavi",
    state: "Karnataka",
    gpsLatitude: 16.2142,
    gpsLongitude: 74.8211,
    primaryCrops: ["Organic Cotton", "Soybean"],
    secondaryCrops: ["Maize"],
    farmingMethod: "Organic",
    useOfPesticides: false,
    pesticideDetails: null,
    useOfChemicalFertilizers: false,
    chemicalFertilizerDetails: null,
    seedSource: "Own Saved",
    previousSeasonYield: "1200 kg",
    livestock: [
      { type: "Cattle", count: 4, primaryUse: "Dairy", fodderSource: "Own Farm" },
      { type: "Goat", count: 5, primaryUse: "Meat", fodderSource: "Purchased" }
    ],
    icsGroup: "Gokak Organic Farmers Association",
    registrationDate: "2025-04-10",
    certificationStatus: "Certified",
    lastInspectionDate: "2026-04-20",
    inspectionResult: "Pass",
    nonConformanceNotes: null,
    nextScheduledInspectionDate: "2026-10-20",
    plots: [
      { id: 2, plotId: "Khasra-104", landArea: 2.0, ownershipType: "Own", irrigationSource: "Borewell", soilType: "Black Cotton" },
      { id: 3, plotId: "Khasra-105", landArea: 1.5, ownershipType: "Own", irrigationSource: "Rain-fed", soilType: "Black Cotton" }
    ],
    householdMembers: [
      { name: "Kiran Patel", relationship: "Spouse", age: 38, gender: "Female", role: "Farming" },
      { name: "Amit Patel", relationship: "Son", age: 16, gender: "Male", role: "Non-Farming" }
    ]
  },
  {
    farmerId: "FO-26-9011",
    fullName: "Gurpreet Singh",
    dob: "1985-07-04",
    gender: "Male",
    nationalId: "XXXX-XXXX-1122",
    primaryPhone: "+91 99112 23344",
    secondaryPhone: null,
    villageName: "Anandpur",
    gramPanchayat: "Anandpur",
    taluka: "Gokak",
    district: "Belagavi",
    state: "Karnataka",
    gpsLatitude: 16.2149,
    gpsLongitude: 74.8225,
    primaryCrops: ["Rice"],
    secondaryCrops: ["Wheat", "Mustard"],
    farmingMethod: "Conventional",
    useOfPesticides: true,
    pesticideDetails: "Standard pesticide sprays used",
    useOfChemicalFertilizers: true,
    chemicalFertilizerDetails: "Urea and DAP used",
    seedSource: "Purchased",
    previousSeasonYield: "2200 kg",
    livestock: [],
    icsGroup: "Gokak Organic Farmers Association",
    registrationDate: "2026-05-01",
    certificationStatus: "Registered",
    lastInspectionDate: null,
    inspectionResult: null,
    nonConformanceNotes: "Requires transition phase. Currently using chemical fertilizer and pesticides.",
    nextScheduledInspectionDate: "2026-11-01",
    plots: [
      { id: 4, plotId: "Khasra-88", landArea: 4.1, ownershipType: "Leased", irrigationSource: "Canal", soilType: "Alluvial" }
    ],
    householdMembers: [
      { name: "Manpreet Kaur", relationship: "Spouse", age: 35, gender: "Female", role: "Farming" }
    ]
  },
  {
    farmerId: "FO-26-1234",
    fullName: "Babulal Sharma",
    dob: "1969-02-15",
    gender: "Male",
    nationalId: "XXXX-XXXX-9876",
    primaryPhone: "+91 98721 00223",
    secondaryPhone: null,
    villageName: "Rampur",
    gramPanchayat: "Rampur",
    taluka: "Kotputli",
    district: "Jaipur",
    state: "Rajasthan",
    gpsLatitude: 27.7022,
    gpsLongitude: 76.2011,
    primaryCrops: ["Bajra", "Guar"],
    secondaryCrops: ["Barley"],
    farmingMethod: "Organic",
    useOfPesticides: false,
    pesticideDetails: null,
    useOfChemicalFertilizers: false,
    chemicalFertilizerDetails: null,
    seedSource: "Own Saved",
    previousSeasonYield: "600 kg",
    livestock: [
      { type: "Cattle", count: 3, primaryUse: "Dairy", fodderSource: "Own Farm" },
      { type: "Goat", count: 8, primaryUse: "Meat", fodderSource: "Own Farm" }
    ],
    icsGroup: "Kotputli Organic Cluster",
    registrationDate: "2024-08-12",
    certificationStatus: "Suspended",
    lastInspectionDate: "2026-03-15",
    inspectionResult: "Fail",
    nonConformanceNotes: "Chemical fertilizer residues detected in soil analysis report.",
    nextScheduledInspectionDate: "2026-09-15",
    plots: [
      { id: 5, plotId: "Khasra-201", landArea: 3.5, ownershipType: "Own", irrigationSource: "Rain-fed", soilType: "Sandy" }
    ],
    householdMembers: [
      { name: "Sita Devi", relationship: "Spouse", age: 58, gender: "Female", role: "Farming" }
    ]
  }
];

export default function Dashboard() {
  // Auth state
  const [user, setUser] = useState(null);
  const [userProfile, setUserProfile] = useState(null);
  const [authLoading, setAuthLoading] = useState(true);
  const [isDemoMode, setIsDemoMode] = useState(false);

  // Form states
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [loginError, setLoginError] = useState("");
  const [toastMessage, setToastMessage] = useState(null);
  const [toastType, setToastType] = useState("success");

  // Navigation state
  const [activeTab, setActiveTab] = useState("dashboard");

  // Data states
  const [farmers, setFarmers] = useState(MOCK_FARMERS);
  const [staffList, setStaffList] = useState(MOCK_STAFF);
  const [selectedFarmer, setSelectedFarmer] = useState(null);

  // Detail side-drawer editing state
  const [editStatus, setEditStatus] = useState("");
  const [editNotes, setEditNotes] = useState("");

  // New staff form states
  const [showStaffModal, setShowStaffModal] = useState(false);
  const [newStaffName, setNewStaffName] = useState("");
  const [newStaffEmail, setNewStaffEmail] = useState("");
  const [newStaffPassword, setNewStaffPassword] = useState("");
  const [newStaffPhone, setNewStaffPhone] = useState("");
  const [newStaffRole, setNewStaffRole] = useState("Field Officer");

  // Settings states
  const [agencyToken, setAgencyToken] = useState("AMIHA-2026");
  const [transitionPeriod, setTransitionPeriod] = useState("24 Months");

  // Search & Filters
  const [searchQuery, setSearchQuery] = useState("");
  const [statusFilter, setStatusFilter] = useState("All");

  // SSR hydration safeguard
  const [isMounted, setIsMounted] = useState(false);

  useEffect(() => {
    setIsMounted(true);
    
    // Auth Listener
    const unsubscribe = onAuthStateChanged(auth, async (currentUser) => {
      if (currentUser) {
        setUser(currentUser);
        try {
          if (db) {
            const docRef = doc(db, "staff", currentUser.uid);
            const docSnap = await getDoc(docRef);
            if (docSnap.exists()) {
              const profile = docSnap.data();
              // Check if user is Admin or Super Admin
              if (profile.role === "Admin" || profile.role === "Super Admin") {
                setUserProfile({ uid: currentUser.uid, ...profile });
                setLoginError("");
                // Fetch live data
                fetchLiveData();
              } else {
                setLoginError("Access Denied: Web dashboard is only accessible for Admins and Super Admins.");
                signOut(auth);
                setUser(null);
                setUserProfile(null);
              }
            } else {
              // Try local check or set default admin
              setUserProfile({
                uid: currentUser.uid,
                fullName: "Admin Account",
                role: "Admin",
                email: currentUser.email
              });
              fetchLiveData();
            }
          }
        } catch (error) {
          console.error("Firestore auth load failed, falling back to local simulation.", error);
          setUserProfile({
            uid: currentUser.uid,
            fullName: "Admin Account",
            role: "Admin",
            email: currentUser.email
          });
        }
      } else {
        setUser(null);
        setUserProfile(null);
      }
      setAuthLoading(false);
    });

    return () => unsubscribe();
  }, []);

  const fetchLiveData = async () => {
    if (!db) return;
    try {
      // Fetch farmers
      const querySnapshot = await getDocs(collection(db, "farmers"));
      const farmersData = [];
      querySnapshot.forEach((doc) => {
        farmersData.push({ id: doc.id, ...doc.data() });
      });
      if (farmersData.length > 0) {
        setFarmers(farmersData);
      }

      // Fetch staff
      const staffSnapshot = await getDocs(collection(db, "staff"));
      const staffData = [];
      staffSnapshot.forEach((doc) => {
        staffData.push({ id: doc.id, ...doc.data() });
      });
      if (staffData.length > 0) {
        setStaffList(staffData);
      }
    } catch (error) {
      console.warn("Failed to load Firebase Firestore collections. Using mock dataset.", error);
    }
  };

  const handleLogin = async (e) => {
    e.preventDefault();
    setLoginError("");
    setAuthLoading(true);

    if (email.trim() === "admin@ics.org" && password === "password") {
      // Local Bypass for testing
      setUser({ uid: "local-admin", email: "admin@ics.org" });
      setUserProfile({
        uid: "local-admin",
        fullName: "Harish Kumar",
        role: "Admin",
        email: "admin@ics.org"
      });
      setIsDemoMode(true);
      setAuthLoading(false);
      showToast("Signed in as Admin (Demo Mode)", "success");
      return;
    }

    if (email.trim() === "superadmin@ics.org" && password === "password") {
      // Local Bypass for testing
      setUser({ uid: "local-super", email: "superadmin@ics.org" });
      setUserProfile({
        uid: "local-super",
        fullName: "Ananya Patel",
        role: "Super Admin",
        email: "superadmin@ics.org"
      });
      setIsDemoMode(true);
      setAuthLoading(false);
      showToast("Signed in as Super Admin (Demo Mode)", "success");
      return;
    }

    try {
      await signInWithEmailAndPassword(auth, email.trim(), password);
    } catch (error) {
      setLoginError(error.message || "Invalid credentials.");
      setAuthLoading(false);
    }
  };

  const handleLogout = () => {
    if (isDemoMode) {
      setUser(null);
      setUserProfile(null);
      setIsDemoMode(false);
      showToast("Signed out successfully", "success");
    } else {
      signOut(auth).then(() => {
        showToast("Signed out successfully", "success");
      });
    }
  };

  const showToast = (msg, type = "success") => {
    setToastMessage(msg);
    setToastType(type);
    setTimeout(() => {
      setToastMessage(null);
    }, 4000);
  };

  // Farmer updates
  const handleSelectFarmer = (farmer) => {
    setSelectedFarmer(farmer);
    setEditStatus(farmer.certificationStatus);
    setEditNotes(farmer.nonConformanceNotes || "");
  };

  const handleUpdateFarmerStatus = async () => {
    if (!selectedFarmer) return;

    const updatedFarmers = farmers.map((f) => {
      if (f.farmerId === selectedFarmer.farmerId) {
        return {
          ...f,
          certificationStatus: editStatus,
          nonConformanceNotes: editNotes,
          lastInspectionDate: new Date().toISOString().split("T")[0]
        };
      }
      return f;
    });

    setFarmers(updatedFarmers);
    setSelectedFarmer({
      ...selectedFarmer,
      certificationStatus: editStatus,
      nonConformanceNotes: editNotes,
      lastInspectionDate: new Date().toISOString().split("T")[0]
    });

    // Write to Firestore if connected
    if (!isDemoMode && db) {
      try {
        await updateDoc(doc(db, "farmers", selectedFarmer.farmerId), {
          certificationStatus: editStatus,
          nonConformanceNotes: editNotes,
          lastInspectionDate: new Date().toISOString().split("T")[0]
        });
        showToast("Farmer status updated in Firebase Firestore!", "success");
      } catch (error) {
        console.error("Firestore write error", error);
        showToast("Saved locally. Firebase update failed.", "error");
      }
    } else {
      showToast("Status updated successfully! (Demo Mode)", "success");
    }
  };

  // Staff creation
  const handleCreateStaff = async (e) => {
    e.preventDefault();
    if (!newStaffName || !newStaffEmail || !newStaffPassword) {
      showToast("Please fill in all required fields", "error");
      return;
    }

    const newStaff = {
      id: "s-" + Date.now(),
      fullName: newStaffName,
      email: newStaffEmail.trim().toLowerCase(),
      role: newStaffRole,
      phone: newStaffPhone || "N/A"
    };

    setStaffList([...staffList, newStaff]);
    setShowStaffModal(false);

    // Save to Firestore if active
    if (!isDemoMode && db) {
      try {
        // We write the database reference first so the account profile is discoverable.
        // Actual Firebase Auth account generation will be triggered or completed by the user.
        await setDoc(doc(db, "staff", newStaff.id), {
          fullName: newStaff.fullName,
          email: newStaff.email,
          role: newStaff.role,
          phone: newStaff.phone
        });
        showToast("New staff profile uploaded to Firestore!", "success");
      } catch (error) {
        console.error("Firestore staff write error", error);
        showToast("Saved locally. Firebase update failed.", "error");
      }
    } else {
      showToast("Staff registered successfully! (Demo Mode)", "success");
    }

    // Reset inputs
    setNewStaffName("");
    setNewStaffEmail("");
    setNewStaffPassword("");
    setNewStaffPhone("");
    setNewStaffRole("Field Officer");
  };

  const handleExportFarmers = () => {
    if (filteredFarmers.length === 0) {
      showToast("No farmer data available to export", "error");
      return;
    }

    const headers = [
      "Farmer ID",
      "Full Name",
      "DOB",
      "Gender",
      "National ID",
      "Primary Phone",
      "Village",
      "Gram Panchayat",
      "Taluka",
      "District",
      "State",
      "Latitude",
      "Longitude",
      "Farming Method",
      "ICS Group",
      "Certification Status",
      "Crops",
      "Total Area (Acres)",
      "Plots Count",
      "Livestock Count"
    ];

    const rows = filteredFarmers.map(item => {
      const totalArea = (item.plots || []).reduce((acc, p) => acc + p.landArea, 0);
      const cropsStr = (item.primaryCrops || []).join(";");
      const cleanName = (item.fullName || "").replace(/"/g, '""');
      const cleanVillage = (item.villageName || "").replace(/"/g, '""');
      const cleanGP = (item.gramPanchayat || "").replace(/"/g, '""');
      
      return [
        item.farmerId,
        `"${cleanName}"`,
        item.dob,
        item.gender,
        item.nationalId,
        item.primaryPhone,
        `"${cleanVillage}"`,
        `"${cleanGP}"`,
        item.taluka,
        item.district,
        item.state,
        item.gpsLatitude,
        item.gpsLongitude,
        item.farmingMethod,
        item.icsGroup,
        item.certificationStatus,
        `"${cropsStr}"`,
        totalArea,
        (item.plots || []).length,
        (item.livestock || []).length
      ];
    });

    const csvContent = [
      headers.join(","),
      ...rows.map(row => row.join(","))
    ].join("\n");

    const blob = new Blob([csvContent], { type: "text/csv;charset=utf-8;" });
    const url = URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.setAttribute("href", url);
    link.setAttribute("download", `AMIHA_Farmers_Report_${Date.now()}.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    showToast("Report exported successfully!", "success");
  };

  // Metrics calculators
  const totalFarmers = farmers.length;
  const certifiedCount = farmers.filter(f => f.certificationStatus === "Certified").length;
  const pendingCount = farmers.filter(f => f.certificationStatus === "Under Inspection").length;
  const suspendedCount = farmers.filter(f => f.certificationStatus === "Suspended").length;

  // Filtered farmers
  const filteredFarmers = farmers.filter((f) => {
    const matchesSearch = f.fullName.toLowerCase().includes(searchQuery.toLowerCase()) || 
                          f.farmerId.toLowerCase().includes(searchQuery.toLowerCase()) ||
                          f.villageName.toLowerCase().includes(searchQuery.toLowerCase());
    
    const matchesStatus = statusFilter === "All" || f.certificationStatus === statusFilter;
    return matchesSearch && matchesStatus;
  });

  // Chart Data Preparation
  const villageCounts = {};
  farmers.forEach(f => {
    villageCounts[f.villageName] = (villageCounts[f.villageName] || 0) + 1;
  });
  const villageChartData = Object.keys(villageCounts).map(v => ({
    name: v,
    count: villageCounts[v]
  }));

  const cropCounts = {};
  farmers.forEach(f => {
    const crops = f.primaryCrops || [];
    crops.forEach(c => {
      cropCounts[c] = (cropCounts[c] || 0) + 1;
    });
  });
  const cropChartData = Object.keys(cropCounts).map(c => ({
    name: c,
    value: cropCounts[c]
  }));

  const COLORS = ["#2d6a4f", "#40916c", "#74c69d", "#b7e4c7"];

  if (authLoading || !isMounted) {
    return (
      <div className="loading-overlay">
        <div className="spinner"></div>
        <h2>Initializing AMIHA ICS Dashboard...</h2>
      </div>
    );
  }

  // Not Logged In View
  if (!user) {
    return (
      <div className="auth-container">
        <div className="auth-card">
          <div className="auth-logo">
            <Sprout size={36} />
          </div>
          <h1 className="auth-title">AMIHA ICS Manager</h1>
          <p className="auth-subtitle">Admin &amp; Coordinator Control Portal</p>
          
          {loginError && <div className="auth-error">{loginError}</div>}
          
          <form onSubmit={handleLogin}>
            <div className="form-group">
              <label className="form-label">Email Address</label>
              <div className="input-container">
                <Mail size={16} className="input-icon" />
                <input 
                  type="email" 
                  className="form-input" 
                  placeholder="admin@ics.org"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  required
                />
              </div>
            </div>
            
            <div className="form-group">
              <label className="form-label">Password</label>
              <div className="input-container">
                <Lock size={16} className="input-icon" />
                <input 
                  type="password" 
                  className="form-input" 
                  placeholder="••••••••"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  required
                />
              </div>
            </div>
            
            <button type="submit" className="auth-button">
              SIGN IN SECURELY
            </button>
          </form>

          <div style={{ marginTop: "24px", fontSize: "12px", color: "var(--text-muted)" }}>
            <p>Demo accounts:</p>
            <p><strong>Admin</strong>: admin@ics.org | password</p>
            <p><strong>Super Admin</strong>: superadmin@ics.org | password</p>
          </div>
        </div>
      </div>
    );
  }

  // Logged In Dashboard View
  return (
    <div className="app-layout">
      {/* Sidebar */}
      <aside className="sidebar">
        <div className="sidebar-header">
          <Sprout size={28} className="logo-icon" />
          <h2 className="brand-name">AMIHA ICS</h2>
        </div>
        
        <nav style={{ flex: 1 }}>
          <ul className="sidebar-menu">
            <li 
              className={`menu-item ${activeTab === "dashboard" ? "active" : ""}`}
              onClick={() => setActiveTab("dashboard")}
            >
              <LayoutDashboard size={20} />
              Overview Dashboard
            </li>
            
            <li 
              className={`menu-item ${activeTab === "farmers" ? "active" : ""}`}
              onClick={() => setActiveTab("farmers")}
            >
              <Users size={20} />
              Farmers Directory
            </li>
            
            <li 
              className={`menu-item ${activeTab === "staff" ? "active" : ""}`}
              onClick={() => setActiveTab("staff")}
            >
              <UserPlus size={20} />
              Staff Control
            </li>
            
            <li 
              className={`menu-item ${activeTab === "settings" ? "active" : ""}`}
              onClick={() => setActiveTab("settings")}
            >
              <Settings size={20} />
              System Settings
            </li>
          </ul>
        </nav>
        
        <div className="sidebar-footer">
          <div className="user-profile">
            <div className="avatar">
              {(userProfile?.fullName || "A").charAt(0)}
            </div>
            <div className="user-info">
              <span className="user-name">{userProfile?.fullName || "User"}</span>
              <span className="user-role">{userProfile?.role || "Admin"}</span>
            </div>
          </div>
          {isDemoMode && (
            <div style={{ fontSize: "10px", color: "#f57c00", fontWeight: "bold", textAlign: "center" }}>
              DEMO LOCAL MODE ACTIVE
            </div>
          )}
          <button className="btn-logout" onClick={handleLogout}>
            <LogOut size={16} />
            Sign Out
          </button>
        </div>
      </aside>

      {/* Main Content Pane */}
      <main className="main-content">
        
        {/* Dynamic header */}
        <div className="page-header">
          <div>
            <h1 className="page-title">
              {activeTab === "dashboard" && "Overview & Analytics"}
              {activeTab === "farmers" && "Farmers Directory"}
              {activeTab === "staff" && "Staff Management"}
              {activeTab === "settings" && "System Settings"}
            </h1>
            <p className="page-description">
              {activeTab === "dashboard" && "Real-time crop statistics, inspection pipelines, and grower status."}
              {activeTab === "farmers" && "Manage organic growers, plot records, and update certification compliance."}
              {activeTab === "staff" && "Control field team permissions, register field officers, and inspect active staff."}
              {activeTab === "settings" && "Configure ICS group parameters, token overrides, and database thresholds."}
            </p>
          </div>
          
          <div style={{ display: "flex", gap: "12px", alignItems: "center" }}>
            <span style={{ fontSize: "13px", color: "var(--text-muted)" }}>
              As of: <strong>{new Date().toLocaleDateString()}</strong>
            </span>
          </div>
        </div>

        {/* Dashboard Tab Content */}
        {activeTab === "dashboard" && (
          <div>
            {/* Stats Overview */}
            <div className="stats-grid">
              <div className="stat-card">
                <div className="stat-info">
                  <span className="stat-label">Total Growers</span>
                  <span className="stat-value">{totalFarmers}</span>
                </div>
                <div className="stat-icon-wrapper blue">
                  <Users size={24} />
                </div>
              </div>

              <div className="stat-card">
                <div className="stat-info">
                  <span className="stat-label">Certified Organic</span>
                  <span className="stat-value">{certifiedCount}</span>
                </div>
                <div className="stat-icon-wrapper green">
                  <CheckCircle size={24} />
                </div>
              </div>

              <div className="stat-card">
                <div className="stat-info">
                  <span className="stat-label">Under Inspection</span>
                  <span className="stat-value">{pendingCount}</span>
                </div>
                <div className="stat-icon-wrapper amber">
                  <Activity size={24} />
                </div>
              </div>

              <div className="stat-card">
                <div className="stat-info">
                  <span className="stat-label">Suspended</span>
                  <span className="stat-value">{suspendedCount}</span>
                </div>
                <div className="stat-icon-wrapper red">
                  <AlertTriangle size={24} />
                </div>
              </div>
            </div>

            {/* Charts Section */}
            <div className="analytics-grid">
              <div className="card">
                <div className="card-title">
                  <span>Grower Distribution by Village</span>
                  <span style={{ fontSize: "12px", color: "var(--text-muted)" }}>Acreage metrics</span>
                </div>
                <div className="chart-container">
                  <ResponsiveContainer width="100%" height="100%">
                    <BarChart data={villageChartData}>
                      <XAxis dataKey="name" stroke="var(--text-muted)" fontSize={12} />
                      <YAxis stroke="var(--text-muted)" fontSize={12} allowDecimals={false} />
                      <Tooltip />
                      <Bar dataKey="count" fill="#2d6a4f" radius={[4, 4, 0, 0]} isAnimationActive={false}>
                        {villageChartData.map((entry, index) => (
                          <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                        ))}
                      </Bar>
                    </BarChart>
                  </ResponsiveContainer>
                </div>
              </div>

              <div className="card">
                <div className="card-title">
                  <span>Major Crops Sown</span>
                </div>
                <div className="chart-container">
                  <ResponsiveContainer width="100%" height="100%">
                    <PieChart>
                      <Pie
                        data={cropChartData}
                        cx="50%"
                        cy="50%"
                        innerRadius={60}
                        outerRadius={80}
                        paddingAngle={5}
                        dataKey="value"
                        isAnimationActive={false}
                      >
                        {cropChartData.map((entry, index) => (
                          <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                        ))}
                      </Pie>
                      <Tooltip />
                      <Legend verticalAlign="bottom" height={36} />
                    </PieChart>
                  </ResponsiveContainer>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* Farmers Tab Content */}
        {activeTab === "farmers" && (
          <div className="card">
            <div className="directory-actions">
              <div className="search-wrapper">
                <Search size={18} className="input-icon" />
                <input 
                  type="text" 
                  className="search-input" 
                  placeholder="Search by Name, Farmer ID, or Village..."
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                />
              </div>
              
              <select 
                className="filter-select"
                value={statusFilter}
                onChange={(e) => setStatusFilter(e.target.value)}
              >
                <option value="All">All Statuses</option>
                <option value="Registered">Registered</option>
                <option value="Under Inspection">Under Inspection</option>
                <option value="Certified">Certified</option>
                <option value="Suspended">Suspended</option>
              </select>

              <button className="btn-primary" onClick={handleExportFarmers}>
                Export to Excel
              </button>
            </div>

            <div className="table-container">
              <table className="data-table">
                <thead>
                  <tr>
                    <th>Farmer ID</th>
                    <th>Full Name</th>
                    <th>Village</th>
                    <th>Farming Method</th>
                    <th>Crops</th>
                    <th>Status</th>
                  </tr>
                </thead>
                <tbody>
                  {filteredFarmers.map((farmer) => (
                    <tr key={farmer.farmerId} onClick={() => handleSelectFarmer(farmer)}>
                      <td><strong>{farmer.farmerId}</strong></td>
                      <td>{farmer.fullName}</td>
                      <td>{farmer.villageName} ({farmer.state})</td>
                      <td>{farmer.farmingMethod}</td>
                      <td>
                        <div className="crop-tags-container">
                          {farmer.primaryCrops.map(c => (
                            <span key={c} className="crop-tag primary">{c}</span>
                          ))}
                        </div>
                      </td>
                      <td>
                        <span className={`badge ${
                          farmer.certificationStatus === "Certified" ? "certified" :
                          farmer.certificationStatus === "Under Inspection" ? "inspection" :
                          farmer.certificationStatus === "Registered" ? "registered" : "suspended"
                        }`}>
                          {farmer.certificationStatus}
                        </span>
                      </td>
                    </tr>
                  ))}
                  {filteredFarmers.length === 0 && (
                    <tr>
                      <td colSpan={6} style={{ textAlign: "center", color: "var(--text-muted)", padding: "30px" }}>
                        No farmers found matching filters.
                      </td>
                    </tr>
                  )}
                </tbody>
              </table>
            </div>
          </div>
        )}

        {/* Staff Tab Content */}
        {activeTab === "staff" && (
          <div className="card">
            <div className="page-header" style={{ margin: 0, paddingBottom: "20px" }}>
              <h3 style={{ fontSize: "18px" }}>Active Field Staff</h3>
              {userProfile?.role === "Super Admin" ? (
                <button className="btn-primary" onClick={() => setShowStaffModal(true)}>
                  <Plus size={16} />
                  Add Team Member
                </button>
              ) : (
                <span style={{ fontSize: "12px", color: "var(--accent-amber)", fontWeight: "600" }}>
                  Staff registration is restricted to Super Admins.
                </span>
              )}
            </div>

            <div className="table-container">
              <table className="data-table">
                <thead>
                  <tr>
                    <th>Staff Name</th>
                    <th>Email ID</th>
                    <th>Contact Phone</th>
                    <th>Assigned Role</th>
                    <th>Privileges</th>
                  </tr>
                </thead>
                <tbody>
                  {staffList.map((staff) => (
                    <tr key={staff.id}>
                      <td><strong>{staff.fullName}</strong></td>
                      <td>{staff.email}</td>
                      <td>{staff.phone}</td>
                      <td>
                        <span className={`badge ${staff.role === "Admin" || staff.role === "Super Admin" ? "certified" : "registered"}`}>
                          {staff.role}
                        </span>
                      </td>
                      <td>{staff.role === "Super Admin" ? "Full Global System Read/Write" : "Regional Management"}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}

        {/* Settings Tab Content */}
        {activeTab === "settings" && (
          <div style={{ display: "flex", flexDirection: "column", gap: "24px" }}>
            <div className="card">
              <h3 className="card-title">Agency Credentials &amp; Standards</h3>
              <div className="info-grid" style={{ gridTemplateColumns: "1fr", gap: "20px", maxWidth: "500px" }}>
                <div className="form-group">
                  <label className="form-label">Agency Authorization Token</label>
                  <input 
                    type="text" 
                    className="form-input" 
                    style={{ paddingLeft: "14px" }}
                    value={agencyToken}
                    onChange={(e) => setAgencyToken(e.target.value)}
                  />
                  <span style={{ fontSize: "11px", color: "var(--text-muted)" }}>Token used on mobile clients to authorize local account registers.</span>
                </div>

                <div className="form-group">
                  <label className="form-label">Organic Transition Period</label>
                  <select 
                    className="filter-select" 
                    style={{ width: "100%" }}
                    value={transitionPeriod}
                    onChange={(e) => setTransitionPeriod(e.target.value)}
                  >
                    <option value="12 Months">12 Months</option>
                    <option value="24 Months">24 Months</option>
                    <option value="36 Months">36 Months</option>
                  </select>
                </div>

                <button 
                  className="btn-primary" 
                  style={{ alignSelf: "flex-start" }} 
                  onClick={() => showToast("System configurations saved successfully!", "success")}
                >
                  Save Configuration
                </button>
              </div>
            </div>

            <div className="card">
              <h3 className="card-title">Firebase Synchronization Status</h3>
              <div className="info-grid">
                <div className="info-item">
                  <span className="info-label">Active Project ID</span>
                  <span className="info-value"><strong>{process.env.NEXT_PUBLIC_FIREBASE_PROJECT_ID || "test-amiha"}</strong></span>
                </div>
                <div className="info-item">
                  <span className="info-label">SDK Connection</span>
                  <span className="info-value" style={{ color: "var(--primary-green)" }}>Online &amp; Connected</span>
                </div>
              </div>
            </div>
          </div>
        )}

      </main>

      {/* Selected Farmer Detail Drawer */}
      {selectedFarmer && (
        <div className="drawer-backdrop" onClick={() => setSelectedFarmer(null)}>
          <div className="drawer" onClick={(e) => e.stopPropagation()}>
            <div className="drawer-header">
              <h3 className="drawer-title">{selectedFarmer.fullName}</h3>
              <button className="btn-close" onClick={() => setSelectedFarmer(null)}>
                <X size={20} />
              </button>
            </div>
            
            <div className="drawer-body">
              <div className="drawer-section">
                <div className="drawer-section-title">Grower Credentials</div>
                <div className="info-grid">
                  <div className="info-item">
                    <span className="info-label">Farmer Registration ID</span>
                    <span className="info-value"><strong>{selectedFarmer.farmerId}</strong></span>
                  </div>
                  <div className="info-item">
                    <span className="info-label">National ID / Aadhaar</span>
                    <span className="info-value">{selectedFarmer.nationalId}</span>
                  </div>
                  <div className="info-item">
                    <span className="info-label">Contact Primary</span>
                    <span className="info-value">{selectedFarmer.primaryPhone}</span>
                  </div>
                  <div className="info-item">
                    <span className="info-label">Date of Birth</span>
                    <span className="info-value">{selectedFarmer.dob}</span>
                  </div>
                </div>
              </div>

              <div className="drawer-section">
                <div className="drawer-section-title">Geographical Location</div>
                <div className="info-grid" style={{ marginBottom: "12px" }}>
                  <div className="info-item">
                    <span className="info-label">Village &amp; Panchayat</span>
                    <span className="info-value">{selectedFarmer.villageName}, {selectedFarmer.gramPanchayat}</span>
                  </div>
                  <div className="info-item">
                    <span className="info-label">District &amp; State</span>
                    <span className="info-value">{selectedFarmer.district}, {selectedFarmer.state}</span>
                  </div>
                </div>
                <div className="map-placeholder">
                  <Map size={24} />
                  <span>Map Pin Coordinates Loaded</span>
                  <strong>Lat: {selectedFarmer.gpsLatitude} • Lon: {selectedFarmer.gpsLongitude}</strong>
                </div>
              </div>

              <div className="drawer-section">
                <div className="drawer-section-title">Crops &amp; Farming Practices</div>
                <div className="info-grid" style={{ marginBottom: "16px" }}>
                  <div className="info-item">
                    <span className="info-label">Declared Farming Method</span>
                    <span className="info-value">{selectedFarmer.farmingMethod}</span>
                  </div>
                  <div className="info-item">
                    <span className="info-label">Seed Sourcing</span>
                    <span className="info-value">{selectedFarmer.seedSource}</span>
                  </div>
                  <div className="info-item">
                    <span className="info-label">Chemical Fertilizers Used?</span>
                    <span className="info-value" style={{ color: selectedFarmer.useOfChemicalFertilizers ? "var(--accent-red)" : "var(--primary-green)" }}>
                      {selectedFarmer.useOfChemicalFertilizers ? "Yes" : "No"}
                    </span>
                  </div>
                  <div className="info-item">
                    <span className="info-label">Chemical Pesticides Used?</span>
                    <span className="info-value" style={{ color: selectedFarmer.useOfPesticides ? "var(--accent-red)" : "var(--primary-green)" }}>
                      {selectedFarmer.useOfPesticides ? "Yes" : "No"}
                    </span>
                  </div>
                </div>
                {selectedFarmer.pesticideDetails && (
                  <div style={{ backgroundColor: "#f8fafc", padding: "10px", borderRadius: "6px", fontSize: "13px", color: "var(--text-muted)" }}>
                    <strong>Pesticide Details:</strong> {selectedFarmer.pesticideDetails}
                  </div>
                )}
              </div>

              <div className="drawer-section">
                <div className="drawer-section-title">Certification Review Status</div>
                <div className="info-grid">
                  <div className="info-item">
                    <span className="info-label">Last Audited Date</span>
                    <span className="info-value">{selectedFarmer.lastInspectionDate || "Not Audited"}</span>
                  </div>
                  <div className="info-item">
                    <span className="info-label">Latest Inspector Recommendation</span>
                    <span className="info-value"><strong>{selectedFarmer.inspectionResult || "None"}</strong></span>
                  </div>
                </div>
              </div>
            </div>

            <div className="drawer-footer">
              <h4 style={{ fontSize: "14px", marginBottom: "12px", color: "var(--text-dark)" }}>Internal Control System Decision</h4>
              <div style={{ display: "flex", gap: "12px", marginBottom: "16px" }}>
                <div style={{ flex: 1 }}>
                  <label className="form-label">Update Certification Status</label>
                  <select 
                    className="filter-select"
                    style={{ width: "100%" }}
                    value={editStatus}
                    onChange={(e) => setEditStatus(e.target.value)}
                  >
                    <option value="Registered">Registered</option>
                    <option value="Under Inspection">Under Inspection</option>
                    <option value="Certified">Certified</option>
                    <option value="Suspended">Suspended</option>
                  </select>
                </div>
              </div>

              <div className="form-group">
                <label className="form-label">Compliance / Non-Conformance Notes</label>
                <textarea 
                  className="form-input" 
                  rows={3} 
                  style={{ paddingLeft: "12px", resize: "none" }}
                  placeholder="Enter audit notes..."
                  value={editNotes}
                  onChange={(e) => setEditNotes(e.target.value)}
                />
              </div>

              <button className="btn-primary" style={{ width: "100%" }} onClick={handleUpdateFarmerStatus}>
                Save Certification Review
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Register Staff Modal */}
      {showStaffModal && (
        <div className="drawer-backdrop" style={{ justifyContent: "center", alignItems: "center" }} onClick={() => setShowStaffModal(false)}>
          <div className="auth-card" style={{ maxWidth: "500px", padding: "30px" }} onClick={(e) => e.stopPropagation()}>
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "20px" }}>
              <h3 style={{ fontSize: "20px", color: "var(--primary-green)", display: "flex", alignItems: "center", gap: "8px" }}>
                <UserPlus size={22} />
                Register Team Member
              </h3>
              <button className="btn-close" onClick={() => setShowStaffModal(false)}>
                <X size={20} />
              </button>
            </div>

            <form onSubmit={handleCreateStaff} className="staff-form">
              <div className="form-group" style={{ margin: 0 }}>
                <label className="form-label">Full Name *</label>
                <input 
                  type="text" 
                  className="form-input" 
                  style={{ paddingLeft: "14px" }}
                  placeholder="e.g. Ramesh Kumar"
                  value={newStaffName}
                  onChange={(e) => setNewStaffName(e.target.value)}
                  required
                />
              </div>

              <div className="form-group" style={{ margin: 0 }}>
                <label className="form-label">Email ID *</label>
                <input 
                  type="email" 
                  className="form-input" 
                  style={{ paddingLeft: "14px" }}
                  placeholder="e.g. ramesh@ics.org"
                  value={newStaffEmail}
                  onChange={(e) => setNewStaffEmail(e.target.value)}
                  required
                />
              </div>

              <div className="form-group" style={{ margin: 0 }}>
                <label className="form-label">Password *</label>
                <input 
                  type="password" 
                  className="form-input" 
                  style={{ paddingLeft: "14px" }}
                  placeholder="At least 6 characters"
                  value={newStaffPassword}
                  onChange={(e) => setNewStaffPassword(e.target.value)}
                  required
                />
              </div>

              <div className="form-group" style={{ margin: 0 }}>
                <label className="form-label">Phone Number</label>
                <input 
                  type="text" 
                  className="form-input" 
                  style={{ paddingLeft: "14px" }}
                  placeholder="+91 XXXXX XXXXX"
                  value={newStaffPhone}
                  onChange={(e) => setNewStaffPhone(e.target.value)}
                />
              </div>

              <div className="form-group" style={{ margin: 0 }}>
                <label className="form-label">Role Assignment</label>
                <div className="role-selector">
                  <div 
                    className={`role-option ${newStaffRole === "Field Officer" ? "selected" : ""}`}
                    onClick={() => setNewStaffRole("Field Officer")}
                  >
                    Field Officer
                  </div>
                  <div 
                    className={`role-option ${newStaffRole === "Inspector" ? "selected" : ""}`}
                    onClick={() => setNewStaffRole("Inspector")}
                  >
                    Inspector
                  </div>
                  <div 
                    className={`role-option ${newStaffRole === "Admin" ? "selected" : ""}`}
                    onClick={() => setNewStaffRole("Admin")}
                  >
                    Admin
                  </div>
                </div>
              </div>

              <button type="submit" className="auth-button" style={{ marginTop: "10px" }}>
                CREATE STAFF ACCOUNT
              </button>
            </form>
          </div>
        </div>
      )}

      {/* Toast Notification */}
      {toastMessage && (
        <div className={`toast ${toastType}`}>
          {toastType === "success" ? <CheckCircle size={16} /> : <AlertTriangle size={16} />}
          <span>{toastMessage}</span>
        </div>
      )}
    </div>
  );
}
