package com.theo.meowbook.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.SubcomposeAsyncImage
import com.theo.meowbook.R
import com.theo.meowbook.api.Order
import com.theo.meowbook.model.Breed
import com.theo.meowbook.model.CatListItem
import com.theo.meowbook.ui.components.ShimmerBox
import com.theo.meowbook.utils.DataState
import com.theo.meowbook.utils.data
import com.theo.meowbook.utils.isFailed
import com.theo.meowbook.utils.isSuccess
import com.theo.meowbook.utils.select


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatListScreen(
    viewModel: CatListViewModel = hiltViewModel()
) {
    val catListState by viewModel.catListFLow.collectAsState()
    val breedsListState by viewModel.breedsFlow.collectAsState()
    var selectedBreedIds by remember { mutableStateOf(emptyList<String>()) }
    var isWithInfoSelected by remember { mutableStateOf(false) }
    var orderBy by remember { mutableStateOf(Order.RANDOM) }
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    val listState = rememberLazyListState()
    val pullToRefreshState = rememberPullToRefreshState()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showBottomSheet by remember { mutableStateOf(false) }

    LaunchedEffect(listState, catListState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisibleItemIndex ->
                if (!catListState.isSuccess || lastVisibleItemIndex == null) {
                    return@collect
                }

                val totalItems = catListState.data.size

                //  10 items -> value to give time for the api to send items
                if (lastVisibleItemIndex >= (totalItems - 10)) {
                    viewModel.loadNextPage(selectedBreedIds, isWithInfoSelected, orderBy)
                }
            }
    }

    Scaffold(
        topBar = {
            CatListTopBar(onFilterClick = {
                if (breedsListState.isSuccess) {
                    showBottomSheet = true
                }
                else if(breedsListState.isFailed) {
                    viewModel.loadBreeds()
                }
            })
        }
    ) { paddingValues ->
        CatListView(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            catListState = catListState,
            viewModel = viewModel,
            selectedBreeds = selectedBreedIds,
            isWithInfoSelected = isWithInfoSelected,
            orderBy = orderBy,
            isRefreshing = isRefreshing,
            listState = listState,
            pullToRefreshState = pullToRefreshState
        )
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = !showBottomSheet },
            sheetState = sheetState
        ) {
            BottomSheetContent(
                orderBy = orderBy,
                isWithInfoSelected = isWithInfoSelected,
                selectedBreedIds = selectedBreedIds,
                breedsList = breedsListState.data,
                onSelectedBreedIdsChange = { newSelectedBreedIds ->
                    selectedBreedIds = newSelectedBreedIds
                },
                onOrderChange = { newOrder -> orderBy = newOrder },
                onWithInfoChange = { isSelected -> isWithInfoSelected = isSelected },
                onApplyFilters = {
                    showBottomSheet = false

                    viewModel.loadCats(selectedBreedIds, isWithInfoSelected, orderBy)
                }
            )
        }
    }
}

@Composable
fun BottomSheetContent(
    modifier: Modifier = Modifier,
    breedsList: List<Breed>,
    orderBy: Order,
    selectedBreedIds: List<String>,
    isWithInfoSelected: Boolean,
    onOrderChange: (Order) -> Unit,
    onWithInfoChange: (Boolean) -> Unit,
    onSelectedBreedIdsChange: (List<String>) -> Unit,
    onApplyFilters: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = stringResource(R.string.cat_list_filter_title), style = MaterialTheme.typography.headlineSmall)

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = stringResource(R.string.cat_list_order_by))

            Order.entries.forEach { order ->
                FilterChip(
                    selected = orderBy == order,
                    onClick = { onOrderChange(order) },
                    label = { Text(text = order.displayName, fontSize = 12.sp) }
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = stringResource(R.string.cat_list_breed_info))

            Switch(checked = isWithInfoSelected, onCheckedChange = onWithInfoChange)
        }

        Text(text = stringResource(R.string.cat_list_filter_breeds))

        LazyColumn(
            modifier = Modifier
                .height(240.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.primaryContainer)
        ) {
            items(breedsList) { breed ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val updated = selectedBreedIds.toMutableList()

                            if (breed.id in updated) {
                                updated.remove(breed.id)
                            } else {
                                updated.add(breed.id)
                            }

                            onSelectedBreedIdsChange(updated)
                        }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = breed.name,
                        fontSize = 16.sp
                    )

                    if (selectedBreedIds.contains(breed.id)) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }

        Button(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .align(Alignment.CenterHorizontally),
            onClick = onApplyFilters
        ) {
            Text(text = stringResource(R.string.cat_list_apply_filters))
        }
    }
}


@Composable
fun CatListTopBar(onFilterClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(vertical = 12.dp, horizontal = 24.dp)
            .background(MaterialTheme.colorScheme.background),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = stringResource(R.string.app_title),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier
                .padding(16.dp)
        )

        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .clickable(
                    onClick = { onFilterClick() }
                )
        ) {
            Icon(
                modifier = Modifier
                    .padding(12.dp)
                    .size(24.dp),
                imageVector = Icons.Filled.FilterAlt,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                contentDescription = null
            )
        }
    }
}

@Composable
fun CatListView(
    modifier: Modifier = Modifier,
    catListState: DataState<List<CatListItem>>,
    viewModel: CatListViewModel,
    selectedBreeds: List<String>,
    isWithInfoSelected: Boolean,
    orderBy: Order,
    isRefreshing: Boolean,
    listState: LazyListState,
    pullToRefreshState: androidx.compose.material3.pulltorefresh.PullToRefreshState,
) {
    PullToRefreshBox(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
        state = pullToRefreshState,
        isRefreshing = isRefreshing,
        onRefresh = {
            viewModel.onPullToRefresh(selectedBreeds, isWithInfoSelected, orderBy)
        },
        indicator = {
            Indicator(
                modifier = Modifier.align(Alignment.TopCenter),
                isRefreshing = isRefreshing,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                state = pullToRefreshState
            )
        },
    ) {
        catListState.select(
            onLoading = { CircularProgressIndicator() },
            onSuccess = {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    items(it) { catItem ->
                        CatItem(catItem = catItem)
                    }
                }
            },
            onFailed = { throwable ->
                ErrorAndEmptyView(
                    message = stringResource(R.string.cat_list_error_message),
                    onRetry = {
                        viewModel.loadCats(
                            selectedBreeds,
                            isWithInfoSelected,
                            orderBy
                        )
                    }
                )
            },
        )
    }
}


@Composable
fun CatItem(
    catItem: CatListItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .padding(horizontal = 36.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .fillMaxWidth()
            .border(
                color = MaterialTheme.colorScheme.primaryContainer,
                width = 2.dp,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        SubcomposeAsyncImage(
            model = catItem.url,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(200.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable(
                    onClick = onClick
                ),
            loading = {
                ShimmerBox(
                    modifier = Modifier
                        .fillMaxSize()

                )
            },
            error = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.errorContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        )

        Box(
            Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .clickable(
                        onClick = onClick
                    )
            ) {
                Icon(
                    modifier = Modifier
                        .padding(12.dp)
                        .size(24.dp),
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    contentDescription = null
                )
            }
        }
    }
}


@Composable
fun ErrorAndEmptyView(
    modifier: Modifier = Modifier,
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(24.dp))

        Button(onClick = onRetry) {
            Icon(Icons.Default.Refresh, contentDescription = null)

            Spacer(Modifier.width(8.dp))

            Text(text = stringResource(R.string.cat_list_retry))
        }
    }
}
