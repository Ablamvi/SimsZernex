using UnityEngine;
using System.Collections.Generic;

namespace SimsZernex.World
{
    /// <summary>
    /// Génère des maisons procéduralement basées sur un seed
    /// </summary>
    public static class HouseGenerator
    {
        public static House GenerateHouse(int seed)
        {
            Random.InitState(seed);

            var houseGo = new GameObject("House_" + seed);
            var house = houseGo.AddComponent<House>();

            // Retourner la maison après initialisation
            return house;
        }

        public static House GenerateRandomHouse()
        {
            return GenerateHouse(Random.Range(0, 999999));
        }
    }

    /// <summary>
    /// Gère la ville avec zones, lieux et Sims autonomes
    /// </summary>
    public class CityManager : MonoBehaviour
    {
        [SerializeField] private int cityWidth = 20;
        [SerializeField] private int cityHeight = 20;
        [SerializeField] private float tileSize = 10f;

        private Location[,] locationGrid;
        private List<Location> locations = new List<Location>();
        private List<SimCharacter> citySimsCharacters = new List<SimCharacter>();

        public int CityWidth => cityWidth;
        public int CityHeight => cityHeight;
        public List<Location> Locations => locations;
        public List<SimCharacter> SimsInCity => citySimsCharacters;

        public static CityManager Instance { get; private set; }

        private void Awake()
        {
            if (Instance != null && Instance != this)
            {
                Destroy(gameObject);
                return;
            }
            Instance = this;
        }

        private void Start()
        {
            locationGrid = new Location[cityWidth, cityHeight];
            GenerateCity();
        }

        private void GenerateCity()
        {
            // Créer les lieux principaux
            CreateLocation("Parc Central", LocationType.Park, 5, 5, 3, 3);
            CreateLocation("Centre commercial", LocationType.Shop, 10, 5, 2, 2);
            CreateLocation("Restaurant", LocationType.Restaurant, 12, 8, 1, 1);
            CreateLocation("Plage", LocationType.Beach, 15, 10, 3, 3);
            CreateLocation("Laboratoire", LocationType.Laboratory, 2, 15, 2, 2);
            CreateLocation("Bureau", LocationType.Workplace, 8, 15, 2, 2);

            Debug.Log("[CityManager] Ville générée avec " + locations.Count + " lieux");
        }

        private void CreateLocation(string name, LocationType type, int startX, int startY, int width, int height)
        {
            var locationGo = new GameObject(name);
            locationGo.transform.SetParent(transform);
            locationGo.transform.position = new Vector3(startX * tileSize, 0, startY * tileSize);

            var location = locationGo.AddComponent<Location>();
            location.Initialize(name, type, startX, startY, width, height, tileSize);

            locations.Add(location);

            // Remplir la grille
            for (int x = startX; x < startX + width && x < cityWidth; x++)
            {
                for (int y = startY; y < startY + height && y < cityHeight; y++)
                {
                    locationGrid[x, y] = location;
                }
            }
        }

        public Location GetLocationAtPosition(Vector3 position)
        {
            int gridX = (int)(position.x / tileSize);
            int gridY = (int)(position.z / tileSize);

            if (gridX >= 0 && gridX < cityWidth && gridY >= 0 && gridY < cityHeight)
                return locationGrid[gridX, gridY];

            return null;
        }

        public Location GetLocationByType(LocationType type)
        {
            foreach (var location in locations)
            {
                if (location.LocationType == type)
                    return location;
            }
            return null;
        }

        public void RegisterSimInCity(SimCharacter sim)
        {
            if (!citySimsCharacters.Contains(sim))
            {
                citySimsCharacters.Add(sim);
                Debug.Log($"[CityManager] {sim.CharacterData.name} enregistré dans la ville");
            }
        }

        public void UnregisterSimInCity(SimCharacter sim)
        {
            citySimsCharacters.Remove(sim);
        }

        public List<SimCharacter> GetSimsNearPosition(Vector3 position, float radius = 10f)
        {
            var nearbySimsChars = new List<SimCharacter>();
            foreach (var sim in citySimsCharacters)
            {
                if (Vector3.Distance(sim.transform.position, position) <= radius)
                    nearbySimsChars.Add(sim);
            }
            return nearbySimsChars;
        }

        public Data.CityState GetCityState()
        {
            var state = new Data.CityState();
            foreach (var sim in citySimsCharacters)
            {
                state.Sims.Add(sim.CharacterData);
            }
            foreach (var location in locations)
            {
                state.Locations.Add(new Data.LocationData
                {
                    LocationName = location.LocationName,
                    Position = location.transform.position,
                    Type = (Data.LocationType)location.LocationType
                });
            }
            return state;
        }
    }

    /// <summary>
    /// Représente un lieu dans la ville (parc, restaurant, etc.)
    /// </summary>
    public class Location : MonoBehaviour
    {
        private string locationName;
        private LocationType locationType;
        private int gridStartX, gridStartY;
        private int gridWidth, gridHeight;
        private float tileSize;
        private List<SimCharacter> simsPresentLocations = new List<SimCharacter>();

        public string LocationName => locationName;
        public LocationType LocationType => locationType;
        public List<SimCharacter> SimsPresent => simsPresentLocations;

        public void Initialize(string name, LocationType type, int startX, int startY, int width, int height, float tileSz)
        {
            locationName = name;
            locationType = type;
            gridStartX = startX;
            gridStartY = startY;
            gridWidth = width;
            gridHeight = height;
            tileSize = tileSz;

            // Créer un objet visuel simple
            var meshFilter = gameObject.AddComponent<MeshFilter>();
            var renderer = gameObject.AddComponent<MeshRenderer>();
            var collider = gameObject.AddComponent<BoxCollider>();

            meshFilter.mesh = CreateLocationMesh();
            renderer.material = new Material(Shader.Find("Standard"));
            renderer.material.color = GetColorByType();
            collider.size = new Vector3(gridWidth * tileSize, 1f, gridHeight * tileSize);
        }

        private Mesh CreateLocationMesh()
        {
            var mesh = new Mesh();
            float w = gridWidth * tileSize * 0.5f;
            float h = gridHeight * tileSize * 0.5f;

            Vector3[] vertices = new Vector3[4]
            {
                new Vector3(-w, 0, -h),
                new Vector3(w, 0, -h),
                new Vector3(w, 0, h),
                new Vector3(-w, 0, h)
            };
            mesh.vertices = vertices;
            mesh.triangles = new int[] { 0, 1, 2, 0, 2, 3 };
            mesh.RecalculateNormals();
            return mesh;
        }

        private Color GetColorByType()
        {
            return locationType switch
            {
                LocationType.Park => new Color(0.2f, 0.8f, 0.2f),
                LocationType.Shop => new Color(0.9f, 0.7f, 0.2f),
                LocationType.Restaurant => new Color(1f, 0.3f, 0.3f),
                LocationType.Beach => new Color(1f, 0.9f, 0.2f),
                LocationType.Laboratory => new Color(0.5f, 0.5f, 1f),
                LocationType.Workplace => new Color(0.7f, 0.7f, 0.7f),
                _ => Color.gray
            };
        }

        public void SimArrive(SimCharacter sim)
        {
            if (!simsPresentLocations.Contains(sim))
            {
                simsPresentLocations.Add(sim);
                Debug.Log($"[Location] {sim.CharacterData.name} arrive à {locationName}");
            }
        }

        public void SimLeave(SimCharacter sim)
        {
            simsPresentLocations.Remove(sim);
        }

        public Vector3 GetRandomPositionInLocation()
        {
            float randomX = gridStartX * tileSize + Random.Range(0, gridWidth * tileSize);
            float randomZ = gridStartY * tileSize + Random.Range(0, gridHeight * tileSize);
            return new Vector3(randomX, 0, randomZ);
        }
    }

    public enum LocationType
    {
        House,
        Workplace,
        Recreation,
        Shop,
        Restaurant,
        Park,
        Beach,
        Laboratory,
        Hospital,
        School,
        Gym
    }
}
