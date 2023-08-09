package iudx.apd.acl.server.apiserver.util;

import java.util.UUID;

/**
 * A class representing a resource object with item ID, provider ID, and resource group ID
 * (optional). This class is used to store information about a resource/resource_group.
 */
public class ResourceObj {
  private final UUID itemId;
  private final UUID providerId;
  private final UUID resourceGroupId;

  /**
   * Constructs a new ResourceObj with the given item ID, provider ID, and resource group ID. If the
   * item is resource group, the resource group ID will be null.
   *
   * @param itemId The unique ID of the resource item.
   * @param providerId The unique ID of the provider who owns the resource.
   * @param resourceGroupId The unique ID of the resource group to which the resource belongs (can
   *     be null).
   */
  public ResourceObj(UUID itemId, UUID providerId, UUID resourceGroupId) {
    this.itemId = itemId;
    this.providerId = providerId;
    // in case of resourceGroup, 'resourceGroupId' will be null
    this.resourceGroupId = resourceGroupId;
  }

  /**
   * Get the item ID of the resource/resource_group.
   *
   * @return The item ID as a UUID.
   */
  public UUID getItemId() {
    return itemId;
  }

  /**
   * Get the provider ID of the resource/resource_group.
   *
   * @return The provider ID as a UUID.
   */
  public UUID getProviderId() {
    return providerId;
  }

  /**
   * Get the resource group ID of the resource.
   *
   * @return The resource group ID as a UUID, or null if the item is resource group.
   */
  public UUID getResourceGroupId() {
    return resourceGroupId;
  }
}