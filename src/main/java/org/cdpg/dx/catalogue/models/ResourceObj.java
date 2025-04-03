package org.cdpg.dx.catalogue.models;

import java.util.UUID;
import org.cdpg.dx.acl.policy.util.ItemType;

/**
 * A class representing a resource object with item ID, provider ID, and resource group ID
 * (optional). This class is used to store information about a resource/resource_group.
 */

/**
 * Constructs a new ResourceObj with the given item ID, provider ID, and resource group ID. If the
 * item is resource group, the resource group ID will be null.
 *
 * @param itemId The unique ID of the resource item.
 * @param providerId The unique ID of the provider who owns the resource.
 * @param resourceGroupId The unique ID of the resource group to which the resource belongs (can be
 *     null).
 * @param resourceServerUrl The resource server URL to which the resource item belong.
 * @param isGroupLevelResource Boolean which is true when the resource is Rs-Group and vice-versa.
 */
public record ResourceObj(
    UUID itemId,
    UUID providerId,
    UUID resourceGroupId,
    String resourceServerUrl,
    boolean isGroupLevelResource) {
  public UUID getResourceGroupId() {
    return isGroupLevelResource() ? null : resourceGroupId();
  }

  public ItemType getItemType() {
    return this.isGroupLevelResource() ? ItemType.RESOURCE_GROUP : ItemType.RESOURCE;
  }
}
