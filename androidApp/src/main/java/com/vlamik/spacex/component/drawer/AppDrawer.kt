package com.vlamik.spacex.component.drawer

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Rocket
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.vlamik.spacex.R
import com.vlamik.spacex.navigation.NavRoutes
import kotlinx.coroutines.launch

@Composable
fun AppDrawer(
    currentRoute: NavRoutes,
    onItemSelected: (NavRoutes) -> Unit,
    drawerState: DrawerState,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        modifier = modifier,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(240.dp) // Standardized width
            ) {
                // Drawer Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.RocketLaunch,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(16.dp))
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                HorizontalDivider()

                // Drawer Items
                LazyColumn(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(NavRoutes.drawerItems) { route ->
                        val (labelRes, icon) = when (route) {
                            NavRoutes.RocketsList -> R.string.rockets to Icons.Default.Rocket
                            NavRoutes.Crew -> R.string.crew to Icons.Default.People
                            else -> null to null
                        }

                        if (labelRes != null && icon != null) {
                            NavigationDrawerItem(
                                label = {
                                    Text(
                                        text = stringResource(labelRes),
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                },
                                selected = currentRoute.path == route.path,
                                onClick = {
                                    scope.launch {
                                        drawerState.close()
                                        onItemSelected(route)
                                    }
                                },
                                icon = {
                                    Icon(
                                        icon,
                                        contentDescription = stringResource(labelRes)
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth(),
                                colors = NavigationDrawerItemDefaults.colors(
                                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    unselectedContainerColor = Color.Transparent
                                )
                            )
                        }
                    }

                    // Footer Section
                    item {

                        Spacer(Modifier.height(24.dp))
                        HorizontalDivider()
                        NavigationDrawerItem(
                            label = { Text(stringResource(R.string.settings)) },
                            icon = { Icon(Icons.Default.Settings, null) },
                            onClick = {
                                scope.launch { drawerState.close() }
                            },
                            selected = false
                        )
                    }
                }
            }
        }
    ) {
        content()
    }
}