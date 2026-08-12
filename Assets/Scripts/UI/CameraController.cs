using UnityEngine;
using SimsZernex.Character;
using SimsZernex.Core;

namespace SimsZernex.UI
{
    /// <summary>
    /// Gère la caméra 3D et les contrôles d'affichage
    /// </summary>
    public class CameraController : MonoBehaviour
    {
        [SerializeField] private float panSpeed = 20f;
        [SerializeField] private float rotationSpeed = 5f;
        [SerializeField] private float zoomSpeed = 10f;
        [SerializeField] private float minZoom = 5f;
        [SerializeField] private float maxZoom = 50f;

        private Camera mainCamera;
        private Vector3 targetPosition;
        private float currentZoom = 15f;
        private SimCharacter followTarget;

        private void Start()
        {
            mainCamera = Camera.main;
            targetPosition = transform.position;
        }

        private void Update()
        {
            HandleInput();
            UpdateCamera();
        }

        private void HandleInput()
        {
            // Panoramique avec ZQSD
            float moveX = Input.GetAxis("Horizontal");
            float moveZ = Input.GetAxis("Vertical");

            if (moveX != 0 || moveZ != 0)
            {
                Vector3 movement = new Vector3(moveX, 0, moveZ).normalized * panSpeed * Time.deltaTime;
                targetPosition += movement;
            }

            // Zoom avec molette
            float scroll = Input.GetAxis("Mouse ScrollWheel");
            if (scroll != 0)
            {
                currentZoom = Mathf.Clamp(currentZoom - scroll * zoomSpeed, minZoom, maxZoom);
            }

            // Rotation avec clic droit
            if (Input.GetMouseButton(1))
            {
                float rotX = Input.GetAxis("Mouse X") * rotationSpeed;
                transform.RotateAround(targetPosition, Vector3.up, rotX);
            }
        }

        private void UpdateCamera()
        {
            if (followTarget != null)
            {
                targetPosition = followTarget.transform.position;
            }

            Vector3 desiredPosition = targetPosition + Vector3.up * currentZoom - transform.forward * currentZoom;
            transform.position = Vector3.Lerp(transform.position, desiredPosition, Time.deltaTime * 5f);
            transform.LookAt(targetPosition + Vector3.up * 2f);
        }

        public void SetFollowTarget(SimCharacter sim)
        {
            followTarget = sim;
        }

        public void ClearFollowTarget()
        {
            followTarget = null;
        }
    }
}
