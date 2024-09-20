import xarray as xr
import numpy as np
import dask

def find_max_wind_at_location(u_file, v_file, lat, lon, altitude_levels, u_var='uwnd', v_var='vwnd'):
    # Charger les fichiers NetCDF
    ds_u = xr.open_dataset(u_file, chunks='auto')
    ds_v = xr.open_dataset(v_file, chunks='auto')

    # Calculer la vitesse du vent
    wind_speed = np.sqrt(ds_u[u_var]**2 + ds_v[v_var]**2)

    # Trouver l'index le plus proche pour la latitude et la longitude de Palaiseau
    lat_idx = abs(ds_u.lat - lat).argmin()
    lon_idx = abs(ds_u.lon - lon).argmin()

    # Extraire les données pour Palaiseau
    palaiseau_wind = wind_speed.isel(lat=lat_idx, lon=lon_idx)

    results = {}
    for alt in altitude_levels:
        # Trouver l'index le plus proche pour le niveau d'altitude
        level_idx = abs(ds_u.level - alt).argmin()
        
        # Calculer le vent maximal à ce niveau
        max_wind = palaiseau_wind.isel(level=level_idx).max()
        
        results[alt] = max_wind

    # Calculer tous les résultats en une seule fois
    computed_results = dask.compute(results)[0]
    return {k: v.item() for k, v in computed_results.items()}

# Coordonnées de Palaiseau
palaiseau_lat = 48.7137
palaiseau_lon = 2.2100

# Niveaux d'altitude (en hPa, à ajuster selon vos données)
altitude_levels = [1000, 50]  # 1000 hPa ~ niveau du sol, 50 hPa ~ 20 km d'altitude

# Chemins des fichiers 
u_file = "B:/Projets/Stratolia/Projet2/uwnd.2000.nc"
v_file = "B:/Projets/Stratolia/Projet2/vwnd.2000.nc"

try:
    max_winds = find_max_wind_at_location(u_file, v_file, palaiseau_lat, palaiseau_lon, altitude_levels)
    
    for alt, wind in max_winds.items():
        print(f"Vent maximal à Palaiseau à {alt} hPa: {wind:.2f} m/s")
except Exception as e:
    print(f"Une erreur s'est produite : {e}")
    print("Vérifiez les chemins des fichiers et la structure des données.")