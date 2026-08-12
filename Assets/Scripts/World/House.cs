using UnityEngine;
using System.Collections.Generic;

namespace SimsZernex.World
{
    /// <summary>
    /// Représente une maison avec pièces, objets interactifs et architecture dynamique
    /// </summary>
    public class House : MonoBehaviour
    {
        [SerializeField] private int gridWidth = 10;
        [SerializeField] private int gridHeight = 10;
        [SerializeField] private float tileSize = 1f;

        private SimCharacter owner;
        private Room[,] roomGrid;
        private List<Room> rooms = new List<Room>();
        private List<InteractiveObject> objects = new List<InteractiveObject>();
        private int houseValue = 50000;
        private string houseName = "Maison de Sim";

        public SimCharacter Owner => owner;
        public int HouseValue => houseValue;
        public List<Room> Rooms => rooms;
        public string HouseName => houseName;

        private void Start()
        {
            roomGrid = new Room[gridWidth, gridHeight];
            CreateDefaultLayout();
        }

        public void SetOwner(SimCharacter newOwner)
        {
            owner = newOwner;
        }

        private void CreateDefaultLayout()
        {
            // Créer un layout de base: chambre, cuisine, salon, salle de bain
            CreateRoom("Chambre", RoomType.Bedroom, 0, 0, 4, 4);
            CreateRoom("Cuisine", RoomType.Kitchen, 5, 0, 4, 4);
            CreateRoom("Salon", RoomType.LivingRoom, 0, 5, 5, 4);
            CreateRoom("Salle de bain", RoomType.Bathroom, 5, 5, 4, 4);
            CreateRoom("Grenier", RoomType.Storage, 0, 9, 10, 1);
        }

        private void CreateRoom(string name, RoomType type, int startX, int startY, int width, int height)
        {
            var roomObject = new GameObject(name);
            roomObject.transform.SetParent(transform);
            roomObject.transform.position = new Vector3(startX * tileSize, 0, startY * tileSize);

            var room = roomObject.AddComponent<Room>();
            room.Initialize(name, type, startX, startY, width, height, tileSize);

            rooms.Add(room);

            // Remplir la grille
            for (int x = startX; x < startX + width && x < gridWidth; x++)
            {
                for (int y = startY; y < startY + height && y < gridHeight; y++)
                {
                    roomGrid[x, y] = room;
                }
            }

            Debug.Log($"[House] Pièce créée: {name} ({type})");
        }

        public void AddObject(InteractiveObject obj)
        {
            objects.Add(obj);
            obj.transform.SetParent(transform);
        }

        public Room GetRoomAtPosition(Vector3 position)
        {
            int gridX = (int)(position.x / tileSize);
            int gridY = (int)(position.z / tileSize);

            if (gridX >= 0 && gridX < gridWidth && gridY >= 0 && gridY < gridHeight)
                return roomGrid[gridX, gridY];

            return null;
        }

        public Room GetRoomByType(RoomType type)
        {
            foreach (var room in rooms)
            {
                if (room.RoomType == type)
                    return room;
            }
            return null;
        }

        public void UpgradeHouse(int costIncrease)
        {
            houseValue += costIncrease;
            Debug.Log($"[House] Maison améliorée! Nouvelle valeur: {houseValue}");
        }

        public Vector3 GetRandomPositionInRoom(Room room)
        {
            if (room == null) return transform.position;
            return room.GetRandomPosition();
        }
    }

    /// <summary>
    /// Représente une pièce de la maison
    /// </summary>
    public class Room : MonoBehaviour
    {
        private string roomName;
        private RoomType roomType;
        private int gridStartX, gridStartY;
        private int gridWidth, gridHeight;
        private float tileSize;
        private List<InteractiveObject> roomObjects = new List<InteractiveObject>();

        public string RoomName => roomName;
        public RoomType RoomType => roomType;
        public List<InteractiveObject> RoomObjects => roomObjects;

        public void Initialize(string name, RoomType type, int startX, int startY, int width, int height, float tileSz)
        {
            roomName = name;
            roomType = type;
            gridStartX = startX;
            gridStartY = startY;
            gridWidth = width;
            gridHeight = height;
            tileSize = tileSz;

            // Créer les objets par défaut selon le type de pièce
            CreateDefaultObjects();
        }

        private void CreateDefaultObjects()
        {
            switch (roomType)
            {
                case RoomType.Bedroom:
                    AddDefaultObject("Lit", InteractiveType.Bed);
                    AddDefaultObject("Lampe", InteractiveType.Light);
                    break;
                case RoomType.Kitchen:
                    AddDefaultObject("Réfrigérateur", InteractiveType.Fridge);
                    AddDefaultObject("Cuisinière", InteractiveType.Stove);
                    AddDefaultObject("Table de cuisine", InteractiveType.Table);
                    break;
                case RoomType.LivingRoom:
                    AddDefaultObject("Canapé", InteractiveType.Sofa);
                    AddDefaultObject("Télévision", InteractiveType.TV);
                    AddDefaultObject("Table basse", InteractiveType.Table);
                    break;
                case RoomType.Bathroom:
                    AddDefaultObject("Douche", InteractiveType.Shower);
                    AddDefaultObject("Toilettes", InteractiveType.Toilet);
                    AddDefaultObject("Lavabo", InteractiveType.Sink);
                    break;
            }
        }

        private void AddDefaultObject(string name, InteractiveType type)
        {
            var objGo = new GameObject(name);
            objGo.transform.SetParent(transform);
            objGo.transform.position = transform.position + new Vector3(Random.Range(0f, gridWidth), 0.5f, Random.Range(0f, gridHeight));

            var interactiveObj = objGo.AddComponent<InteractiveObject>();
            interactiveObj.Initialize(name, type, 50); // Durabilité 50
            roomObjects.Add(interactiveObj);
        }

        public Vector3 GetRandomPosition()
        {
            float randomX = gridStartX + Random.Range(0.5f, gridWidth - 0.5f);
            float randomZ = gridStartY + Random.Range(0.5f, gridHeight - 0.5f);
            return new Vector3(randomX * tileSize, 0, randomZ * tileSize);
        }

        public InteractiveObject GetObjectByType(InteractiveType type)
        {
            foreach (var obj in roomObjects)
            {
                if (obj.InteractiveType == type)
                    return obj;
            }
            return null;
        }
    }

    /// <summary>
    /// Objet interactif dans une pièce (lit, toilettes, TV, etc.)
    /// </summary>
    public class InteractiveObject : MonoBehaviour
    {
        private string objectName;
        private InteractiveType interactiveType;
        private float durability = 100f; // 0-100
        private bool isInUse = false;
        private SimCharacter currentUser;

        public string ObjectName => objectName;
        public InteractiveType InteractiveType => interactiveType;
        public float Durability => durability;
        public bool IsInUse => isInUse;
        public SimCharacter CurrentUser => currentUser;

        public void Initialize(string name, InteractiveType type, float startDurability = 100f)
        {
            objectName = name;
            interactiveType = type;
            durability = startDurability;

            // Créer un simple mesh visuel
            var mesh = gameObject.AddComponent<MeshFilter>();
            var renderer = gameObject.AddComponent<MeshRenderer>();
            var collider = gameObject.AddComponent<BoxCollider>();

            mesh.mesh = CreateSimpleMesh();
            renderer.material = new Material(Shader.Find("Standard"));
            renderer.material.color = GetColorByType();
            collider.size = GetColliderSizeByType();
        }

        private Mesh CreateSimpleMesh()
        {
            var mesh = new Mesh();
            Vector3[] vertices = new Vector3[8]
            {
                Vector3.zero,
                Vector3.right * 0.8f,
                Vector3.right * 0.8f + Vector3.up * 0.8f,
                Vector3.up * 0.8f,
                Vector3.forward * 0.8f,
                Vector3.right * 0.8f + Vector3.forward * 0.8f,
                Vector3.right * 0.8f + Vector3.up * 0.8f + Vector3.forward * 0.8f,
                Vector3.up * 0.8f + Vector3.forward * 0.8f
            };
            mesh.vertices = vertices;

            int[] triangles = new int[36]
            {
                0, 2, 1, 0, 3, 2,
                4, 5, 6, 4, 6, 7,
                0, 1, 5, 0, 5, 4,
                2, 3, 7, 2, 7, 6,
                0, 4, 7, 0, 7, 3,
                1, 2, 6, 1, 6, 5
            };
            mesh.triangles = triangles;
            mesh.RecalculateNormals();
            return mesh;
        }

        private Color GetColorByType()
        {
            return interactiveType switch
            {
                InteractiveType.Bed => new Color(0.6f, 0.3f, 0.2f),
                InteractiveType.Toilet => new Color(0.9f, 0.9f, 1f),
                InteractiveType.Shower => new Color(0.5f, 0.7f, 1f),
                InteractiveType.Stove => new Color(0.3f, 0.3f, 0.3f),
                InteractiveType.Fridge => new Color(0.8f, 0.8f, 0.8f),
                InteractiveType.TV => new Color(0.1f, 0.1f, 0.2f),
                InteractiveType.Sofa => new Color(0.7f, 0.4f, 0.4f),
                _ => new Color(0.6f, 0.6f, 0.6f)
            };
        }

        private Vector3 GetColliderSizeByType()
        {
            return interactiveType switch
            {
                InteractiveType.Bed => new Vector3(0.8f, 0.3f, 1.6f),
                InteractiveType.Sofa => new Vector3(2f, 0.8f, 1f),
                InteractiveType.Table => new Vector3(1.2f, 0.8f, 1.2f),
                _ => Vector3.one
            };
        }

        public void UseObject(SimCharacter user)
        {
            if (isInUse) return;

            isInUse = true;
            currentUser = user;
            durability -= 2f; // Usure

            Debug.Log($"[InteractiveObject] {user.CharacterData.name} utilise {objectName}");
        }

        public void StopUsing()
        {
            isInUse = false;
            currentUser = null;
        }

        public void Repair()
        {
            durability = 100f;
        }
    }

    public enum RoomType
    {
        Bedroom,
        Kitchen,
        LivingRoom,
        Bathroom,
        DiningRoom,
        Office,
        Gym,
        Recreation,
        Storage
    }

    public enum InteractiveType
    {
        Bed,
        Toilet,
        Shower,
        Sink,
        Stove,
        Fridge,
        Microwave,
        TV,
        Computer,
        Sofa,
        Table,
        Chair,
        Light,
        Door,
        Bookshelf,
        Instrument
    }
}
