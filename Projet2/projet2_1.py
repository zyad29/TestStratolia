import xarray as xr
import numpy as np
import pandas as pd
import dask
import os

def process_wind_data_chunked(u_file, v_file, u_var='uwnd', v_var='vwnd'):
    # Charger les fichiers NetCDF en laissant xarray déterminer le meilleur chunking
    ds_u = xr.open_dataset(u_file, chunks='auto')
    ds_v = xr.open_dataset(v_file, chunks='auto')

    # Vérifier si les variables existent
    if u_var not in ds_u or v_var not in ds_v:
        raise ValueError(f"Variables {u_var} ou {v_var} non trouvées dans les fichiers.")

    # Calculer la moyenne annuelle pour u et v séparément
    annual_mean_u = ds_u[u_var].mean(dim='time')
    annual_mean_v = ds_v[v_var].mean(dim='time')

    # Convertir les résultats en DataFrames pandas
    df_u = annual_mean_u.to_dataframe(name='U_Vent_Moyen').reset_index()
    df_v = annual_mean_v.to_dataframe(name='V_Vent_Moyen').reset_index()

    print("Structure des morceaux pour u_file:")
    print(ds_u.chunks)
    print("Structure des morceaux pour v_file:")
    print(ds_v.chunks)

    return df_u, df_v

def process_and_save_chunks(u_file, v_file, output_file_u, output_file_v, u_var='uwnd', v_var='vwnd'):
    df_u_chunks, df_v_chunks = process_wind_data_chunked(u_file, v_file, u_var, v_var)
    
    # Écrire l'en-tête des fichiers CSV
    df_u_chunks.head(0).to_csv(output_file_u, index=False)
    df_v_chunks.head(0).to_csv(output_file_v, index=False)
    
    # Traiter et écrire les données par morceaux pour u
    for chunk in df_u_chunks.itertuples(index=False):
        with open(output_file_u, 'a') as f:
            np.savetxt(f, [chunk], delimiter=',', fmt='%g')

    # Traiter et écrire les données par morceaux pour v
    for chunk in df_v_chunks.itertuples(index=False):
        with open(output_file_v, 'a') as f:
            np.savetxt(f, [chunk], delimiter=',', fmt='%g')

# Utilisation des fonctions avec gestion des chemins
base_dir = "B:/Projets/Stratolia/Projet2"
u_file = os.path.join(base_dir, "uwnd.2000.nc")
v_file = os.path.join(base_dir, "vwnd.2000.nc")
output_file_u = os.path.join(base_dir, "vent_dataset_annuel_u.csv")
output_file_v = os.path.join(base_dir, "vent_dataset_annuel_v.csv")

try:
    process_and_save_chunks(u_file, v_file, output_file_u, output_file_v, u_var='uwnd', v_var='vwnd')
    print(f"Les datasets ont été traités et sauvegardés dans '{output_file_u}' et '{output_file_v}'")
except Exception as e:
    print(f"Une erreur s'est produite : {e}")
    print("Vérifiez les noms des variables dans vos fichiers NetCDF.")