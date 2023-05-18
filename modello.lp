\ LP format - for model browsing. Use MPS format to capture full model detail.
Minimize
  4 u_1,2,1 + 2 u_1,2,2 + u_1,2,3 + 0.5 u_1,2,4 + 0.25 u_1,2,5 + 6 u_1,3,1
   + 3 u_1,3,2 + 1.5 u_1,3,3 + 0.75 u_1,3,4 + 0.375 u_1,3,5 + 4 u_2,1,1
   + 2 u_2,1,2 + u_2,1,3 + 0.5 u_2,1,4 + 0.25 u_2,1,5 + 4 u_2,3,1
   + 2 u_2,3,2 + u_2,3,3 + 0.5 u_2,3,4 + 0.25 u_2,3,5 + 6 u_3,1,1
   + 3 u_3,1,2 + 1.5 u_3,1,3 + 0.75 u_3,1,4 + 0.375 u_3,1,5 + 4 u_3,2,1
   + 2 u_3,2,2 + u_3,2,3 + 0.5 u_3,2,4 + 0.25 u_3,2,5
Subject To
 Vincolo1_e0: y_1,1 + y_1,2 + y_1,3 + y_1,4 + y_1,5 + y_1,6 = 1
 Vincolo1_e1: y_2,1 + y_2,2 + y_2,3 + y_2,4 + y_2,5 + y_2,6 = 1
 Vincolo1_e2: y_3,1 + y_3,2 + y_3,3 + y_3,4 + y_3,5 + y_3,6 = 1
 Vincolo1_e3: y_4,1 + y_4,2 + y_4,3 + y_4,4 + y_4,5 + y_4,6 = 1
 Vincolo2_e0,e1,t0: y_1,1 + y_2,1 <= 1
 Vincolo2_e0,e1,t1: y_1,2 + y_2,2 <= 1
 Vincolo2_e0,e1,t2: y_1,3 + y_2,3 <= 1
 Vincolo2_e0,e1,t3: y_1,4 + y_2,4 <= 1
 Vincolo2_e0,e1,t4: y_1,5 + y_2,5 <= 1
 Vincolo2_e0,e1,t5: y_1,6 + y_2,6 <= 1
 Vincolo2_e0,e2,t0: y_1,1 + y_3,1 <= 1
 Vincolo2_e0,e2,t1: y_1,2 + y_3,2 <= 1
 Vincolo2_e0,e2,t2: y_1,3 + y_3,3 <= 1
 Vincolo2_e0,e2,t3: y_1,4 + y_3,4 <= 1
 Vincolo2_e0,e2,t4: y_1,5 + y_3,5 <= 1
 Vincolo2_e0,e2,t5: y_1,6 + y_3,6 <= 1
 Vincolo2_e1,e0,t0: y_1,1 + y_2,1 <= 1
 Vincolo2_e1,e0,t1: y_1,2 + y_2,2 <= 1
 Vincolo2_e1,e0,t2: y_1,3 + y_2,3 <= 1
 Vincolo2_e1,e0,t3: y_1,4 + y_2,4 <= 1
 Vincolo2_e1,e0,t4: y_1,5 + y_2,5 <= 1
 Vincolo2_e1,e0,t5: y_1,6 + y_2,6 <= 1
 Vincolo2_e1,e2,t0: y_2,1 + y_3,1 <= 1
 Vincolo2_e1,e2,t1: y_2,2 + y_3,2 <= 1
 Vincolo2_e1,e2,t2: y_2,3 + y_3,3 <= 1
 Vincolo2_e1,e2,t3: y_2,4 + y_3,4 <= 1
 Vincolo2_e1,e2,t4: y_2,5 + y_3,5 <= 1
 Vincolo2_e1,e2,t5: y_2,6 + y_3,6 <= 1
 Vincolo2_e2,e0,t0: y_1,1 + y_3,1 <= 1
 Vincolo2_e2,e0,t1: y_1,2 + y_3,2 <= 1
 Vincolo2_e2,e0,t2: y_1,3 + y_3,3 <= 1
 Vincolo2_e2,e0,t3: y_1,4 + y_3,4 <= 1
 Vincolo2_e2,e0,t4: y_1,5 + y_3,5 <= 1
 Vincolo2_e2,e0,t5: y_1,6 + y_3,6 <= 1
 Vincolo2_e2,e1,t0: y_2,1 + y_3,1 <= 1
 Vincolo2_e2,e1,t1: y_2,2 + y_3,2 <= 1
 Vincolo2_e2,e1,t2: y_2,3 + y_3,3 <= 1
 Vincolo2_e2,e1,t3: y_2,4 + y_3,4 <= 1
 Vincolo2_e2,e1,t4: y_2,5 + y_3,5 <= 1
 Vincolo2_e2,e1,t5: y_2,6 + y_3,6 <= 1
 Vincolo3_e0,e1,t0,k0: y_1,1 + y_2,2 - u_1,2,1 <= 1
 Vincolo3_e0,e1,t1,k0: y_1,2 + y_2,3 - u_1,2,1 <= 1
 Vincolo3_e0,e1,t2,k0: y_1,3 + y_2,4 - u_1,2,1 <= 1
 Vincolo3_e0,e1,t3,k0: y_1,4 + y_2,5 - u_1,2,1 <= 1
 Vincolo3_e0,e1,t4,k0: y_1,5 + y_2,6 - u_1,2,1 <= 1
 Vincolo3_e0,e1,t0,k1: y_1,1 + y_2,3 - u_1,2,2 <= 1
 Vincolo3_e0,e1,t1,k1: y_1,2 + y_2,4 - u_1,2,2 <= 1
 Vincolo3_e0,e1,t2,k1: y_1,3 + y_2,5 - u_1,2,2 <= 1
 Vincolo3_e0,e1,t3,k1: y_1,4 + y_2,6 - u_1,2,2 <= 1
 Vincolo3_e0,e1,t0,k2: y_1,1 + y_2,4 - u_1,2,3 <= 1
 Vincolo3_e0,e1,t1,k2: y_1,2 + y_2,5 - u_1,2,3 <= 1
 Vincolo3_e0,e1,t2,k2: y_1,3 + y_2,6 - u_1,2,3 <= 1
 Vincolo3_e0,e1,t0,k3: y_1,1 + y_2,5 - u_1,2,4 <= 1
 Vincolo3_e0,e1,t1,k3: y_1,2 + y_2,6 - u_1,2,4 <= 1
 Vincolo3_e0,e1,t0,k4: y_1,1 + y_2,6 - u_1,2,5 <= 1
 Vincolo3_e0,e2,t0,k0: y_1,1 + y_3,2 - u_1,3,1 <= 1
 Vincolo3_e0,e2,t1,k0: y_1,2 + y_3,3 - u_1,3,1 <= 1
 Vincolo3_e0,e2,t2,k0: y_1,3 + y_3,4 - u_1,3,1 <= 1
 Vincolo3_e0,e2,t3,k0: y_1,4 + y_3,5 - u_1,3,1 <= 1
 Vincolo3_e0,e2,t4,k0: y_1,5 + y_3,6 - u_1,3,1 <= 1
 Vincolo3_e0,e2,t0,k1: y_1,1 + y_3,3 - u_1,3,2 <= 1
 Vincolo3_e0,e2,t1,k1: y_1,2 + y_3,4 - u_1,3,2 <= 1
 Vincolo3_e0,e2,t2,k1: y_1,3 + y_3,5 - u_1,3,2 <= 1
 Vincolo3_e0,e2,t3,k1: y_1,4 + y_3,6 - u_1,3,2 <= 1
 Vincolo3_e0,e2,t0,k2: y_1,1 + y_3,4 - u_1,3,3 <= 1
 Vincolo3_e0,e2,t1,k2: y_1,2 + y_3,5 - u_1,3,3 <= 1
 Vincolo3_e0,e2,t2,k2: y_1,3 + y_3,6 - u_1,3,3 <= 1
 Vincolo3_e0,e2,t0,k3: y_1,1 + y_3,5 - u_1,3,4 <= 1
 Vincolo3_e0,e2,t1,k3: y_1,2 + y_3,6 - u_1,3,4 <= 1
 Vincolo3_e0,e2,t0,k4: y_1,1 + y_3,6 - u_1,3,5 <= 1
 Vincolo3_e1,e0,t0,k0: y_1,2 + y_2,1 - u_2,1,1 <= 1
 Vincolo3_e1,e0,t1,k0: y_1,3 + y_2,2 - u_2,1,1 <= 1
 Vincolo3_e1,e0,t2,k0: y_1,4 + y_2,3 - u_2,1,1 <= 1
 Vincolo3_e1,e0,t3,k0: y_1,5 + y_2,4 - u_2,1,1 <= 1
 Vincolo3_e1,e0,t4,k0: y_1,6 + y_2,5 - u_2,1,1 <= 1
 Vincolo3_e1,e0,t0,k1: y_1,3 + y_2,1 - u_2,1,2 <= 1
 Vincolo3_e1,e0,t1,k1: y_1,4 + y_2,2 - u_2,1,2 <= 1
 Vincolo3_e1,e0,t2,k1: y_1,5 + y_2,3 - u_2,1,2 <= 1
 Vincolo3_e1,e0,t3,k1: y_1,6 + y_2,4 - u_2,1,2 <= 1
 Vincolo3_e1,e0,t0,k2: y_1,4 + y_2,1 - u_2,1,3 <= 1
 Vincolo3_e1,e0,t1,k2: y_1,5 + y_2,2 - u_2,1,3 <= 1
 Vincolo3_e1,e0,t2,k2: y_1,6 + y_2,3 - u_2,1,3 <= 1
 Vincolo3_e1,e0,t0,k3: y_1,5 + y_2,1 - u_2,1,4 <= 1
 Vincolo3_e1,e0,t1,k3: y_1,6 + y_2,2 - u_2,1,4 <= 1
 Vincolo3_e1,e0,t0,k4: y_1,6 + y_2,1 - u_2,1,5 <= 1
 Vincolo3_e1,e2,t0,k0: y_2,1 + y_3,2 - u_2,3,1 <= 1
 Vincolo3_e1,e2,t1,k0: y_2,2 + y_3,3 - u_2,3,1 <= 1
 Vincolo3_e1,e2,t2,k0: y_2,3 + y_3,4 - u_2,3,1 <= 1
 Vincolo3_e1,e2,t3,k0: y_2,4 + y_3,5 - u_2,3,1 <= 1
 Vincolo3_e1,e2,t4,k0: y_2,5 + y_3,6 - u_2,3,1 <= 1
 Vincolo3_e1,e2,t0,k1: y_2,1 + y_3,3 - u_2,3,2 <= 1
 Vincolo3_e1,e2,t1,k1: y_2,2 + y_3,4 - u_2,3,2 <= 1
 Vincolo3_e1,e2,t2,k1: y_2,3 + y_3,5 - u_2,3,2 <= 1
 Vincolo3_e1,e2,t3,k1: y_2,4 + y_3,6 - u_2,3,2 <= 1
 Vincolo3_e1,e2,t0,k2: y_2,1 + y_3,4 - u_2,3,3 <= 1
 Vincolo3_e1,e2,t1,k2: y_2,2 + y_3,5 - u_2,3,3 <= 1
 Vincolo3_e1,e2,t2,k2: y_2,3 + y_3,6 - u_2,3,3 <= 1
 Vincolo3_e1,e2,t0,k3: y_2,1 + y_3,5 - u_2,3,4 <= 1
 Vincolo3_e1,e2,t1,k3: y_2,2 + y_3,6 - u_2,3,4 <= 1
 Vincolo3_e1,e2,t0,k4: y_2,1 + y_3,6 - u_2,3,5 <= 1
 Vincolo3_e2,e0,t0,k0: y_1,2 + y_3,1 - u_3,1,1 <= 1
 Vincolo3_e2,e0,t1,k0: y_1,3 + y_3,2 - u_3,1,1 <= 1
 Vincolo3_e2,e0,t2,k0: y_1,4 + y_3,3 - u_3,1,1 <= 1
 Vincolo3_e2,e0,t3,k0: y_1,5 + y_3,4 - u_3,1,1 <= 1
 Vincolo3_e2,e0,t4,k0: y_1,6 + y_3,5 - u_3,1,1 <= 1
 Vincolo3_e2,e0,t0,k1: y_1,3 + y_3,1 - u_3,1,2 <= 1
 Vincolo3_e2,e0,t1,k1: y_1,4 + y_3,2 - u_3,1,2 <= 1
 Vincolo3_e2,e0,t2,k1: y_1,5 + y_3,3 - u_3,1,2 <= 1
 Vincolo3_e2,e0,t3,k1: y_1,6 + y_3,4 - u_3,1,2 <= 1
 Vincolo3_e2,e0,t0,k2: y_1,4 + y_3,1 - u_3,1,3 <= 1
 Vincolo3_e2,e0,t1,k2: y_1,5 + y_3,2 - u_3,1,3 <= 1
 Vincolo3_e2,e0,t2,k2: y_1,6 + y_3,3 - u_3,1,3 <= 1
 Vincolo3_e2,e0,t0,k3: y_1,5 + y_3,1 - u_3,1,4 <= 1
 Vincolo3_e2,e0,t1,k3: y_1,6 + y_3,2 - u_3,1,4 <= 1
 Vincolo3_e2,e0,t0,k4: y_1,6 + y_3,1 - u_3,1,5 <= 1
 Vincolo3_e2,e1,t0,k0: y_2,2 + y_3,1 - u_3,2,1 <= 1
 Vincolo3_e2,e1,t1,k0: y_2,3 + y_3,2 - u_3,2,1 <= 1
 Vincolo3_e2,e1,t2,k0: y_2,4 + y_3,3 - u_3,2,1 <= 1
 Vincolo3_e2,e1,t3,k0: y_2,5 + y_3,4 - u_3,2,1 <= 1
 Vincolo3_e2,e1,t4,k0: y_2,6 + y_3,5 - u_3,2,1 <= 1
 Vincolo3_e2,e1,t0,k1: y_2,3 + y_3,1 - u_3,2,2 <= 1
 Vincolo3_e2,e1,t1,k1: y_2,4 + y_3,2 - u_3,2,2 <= 1
 Vincolo3_e2,e1,t2,k1: y_2,5 + y_3,3 - u_3,2,2 <= 1
 Vincolo3_e2,e1,t3,k1: y_2,6 + y_3,4 - u_3,2,2 <= 1
 Vincolo3_e2,e1,t0,k2: y_2,4 + y_3,1 - u_3,2,3 <= 1
 Vincolo3_e2,e1,t1,k2: y_2,5 + y_3,2 - u_3,2,3 <= 1
 Vincolo3_e2,e1,t2,k2: y_2,6 + y_3,3 - u_3,2,3 <= 1
 Vincolo3_e2,e1,t0,k3: y_2,5 + y_3,1 - u_3,2,4 <= 1
 Vincolo3_e2,e1,t1,k3: y_2,6 + y_3,2 - u_3,2,4 <= 1
 Vincolo3_e2,e1,t0,k4: y_2,6 + y_3,1 - u_3,2,5 <= 1
Bounds
Binaries
 y_1,1 y_1,2 y_1,3 y_1,4 y_1,5 y_1,6 y_2,1 y_2,2 y_2,3 y_2,4 y_2,5 y_2,6
 y_3,1 y_3,2 y_3,3 y_3,4 y_3,5 y_3,6 y_4,1 y_4,2 y_4,3 y_4,4 y_4,5 y_4,6
End
