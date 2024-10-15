package com.example

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.manga.MangaDetailScreen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import coil.request.ImageRequest
import com.example.manga.R
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults

import kotlinx.coroutines.launch

@Composable
fun Manga() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "mangaList") {
        composable("mangaList") {
            MangaListScreen(navController)
        }
        composable("mangaDetail/{mangaId}") { backStackEntry ->
            val mangaId = remember { backStackEntry.arguments?.getString("mangaId") }
            MangaDetailScreen(mangaId = mangaId?.toIntOrNull(), navController = navController)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MangaListScreen(navController: NavController, viewModel: MangaViewModel = hiltViewModel()) {
    val mangaListState by viewModel.mangaListState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = isRefreshing)
    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val showTopBarTitle by remember {
        derivedStateOf { lazyListState.firstVisibleItemIndex > 0 }
    }

    val gradientColors = listOf(
        Color(0xFFFF9A8B),
        Color(0xFFFF6B6B),
        Color(0xFFFCA3CC)
    )

    Scaffold(
        topBar = {
            EnhancedTopAppBar(
                title = "Funky Manga",
                showTitle = showTopBarTitle,
                onRefresh = { viewModel.refreshMangaList() },
                gradientColors = gradientColors
            )
        },
        bottomBar = {
            EnhancedBottomNavigationBar(viewModel, gradientColors)
        },
        floatingActionButton = {
            BouncingFAB {
                coroutineScope.launch {
                    lazyListState.animateScrollToItem(0)
                }
            }
        }
    ) { innerPadding ->
        SwipeRefresh(
            state = swipeRefreshState,
            onRefresh = { viewModel.refreshMangaList() },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Brush.verticalGradient(gradientColors))
        ) {
            when (val state = mangaListState) {
                is MangaListState.Loading -> LoadingView()
                is MangaListState.Success -> MangaList(state.data, navController, lazyListState)
                is MangaListState.Error -> ErrorView(state.message) { viewModel.fetchTopManga(viewModel.currentPage) }
            }
        }
    }
}

@Composable
fun LoadingView() {
    Box(modifier = Modifier.fillMaxSize()) {
        val infiniteTransition = rememberInfiniteTransition()
        val angle by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = LinearEasing)
            )
        )

        Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = "Loading",
            modifier = Modifier
                .size(100.dp)
                .rotate(angle)
                .align(Alignment.Center),
            tint = Color.White
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MangaList(mangaList: List<Manga>, navController: NavController, lazyListState: LazyListState) {
    LazyColumn(
        state = lazyListState,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        itemsIndexed(mangaList) { index, manga ->
            EnhancedMangaCard(
                manga = manga,
                onItemClick = { navController.navigate("mangaDetail/${manga.id}") },
                modifier = Modifier
                    .animateItemPlacement(
                        animationSpec = tween(durationMillis = 500)
                    )
                    .graphicsLayer(
                        scaleX = 0.9f,
                        scaleY = 0.9f
                    )
                    .animateContentSize()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedMangaCard(manga: Manga, onItemClick: () -> Unit, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    val rotationState = remember { Animatable(0f) }
    val scaleState = remember { Animatable(1f) }

    LaunchedEffect(expanded) {
        rotationState.animateTo(
            targetValue = if (expanded) 5f else 0f,
            animationSpec = spring(stiffness = Spring.StiffnessLow)
        )
        scaleState.animateTo(
            targetValue = if (expanded) 1.05f else 1f,
            animationSpec = spring(stiffness = Spring.StiffnessLow)
        )
    }

    Card(
        onClick = {
            expanded = !expanded
            if (!expanded) onItemClick()
        },
        modifier = modifier
            .graphicsLayer(
                rotationZ = rotationState.value,
                scaleX = scaleState.value,
                scaleY = scaleState.value
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.8f))
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            AsyncImage(
                model = manga.images.jpg.image_url,
                contentDescription = "${manga.title} cover",
                modifier = Modifier
                    .width(120.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)),
                contentScale = ContentScale.Crop
            )
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = manga.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(8.dp))
                MangaRating(score = manga.score)
                Spacer(modifier = Modifier.height(8.dp))
                MangaGenres(genres = manga.genres)
                Spacer(modifier = Modifier.height(12.dp))
                if (expanded) {
                    manga.synopsis?.let {
                        Text(
                            text = it,
                            fontSize = 14.sp,
                            color = Color.Black.copy(alpha = 0.7f),
                            modifier = Modifier.animateContentSize()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MangaRating(score: Float?) {
    score?.let {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val starRotation = remember { Animatable(0f) }
            LaunchedEffect(Unit) {
                starRotation.animateTo(
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2000, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    )
                )
            }
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = "Score",
                tint = Color(0xFFFFC107),
                modifier = Modifier
                    .size(24.dp)
                    .rotate(starRotation.value)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = String.format("%.2f", it),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFC107)
            )
        }
    }
}

@Composable
fun MangaGenres(genres: List<Genre>) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        genres.take(3).forEach { genre ->
            val bounceState = remember { Animatable(1f) }
            LaunchedEffect(Unit) {
                bounceState.animateTo(
                    targetValue = 1.1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(500, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    )
                )
            }
            Chip(
                onClick = { /* TODO: Filter by genre */ },
                colors = ChipDefaults.chipColors(
                    backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                label = { Text(genre.name, fontSize = 12.sp) },
                modifier = Modifier.scale(bounceState.value)
            )
        }
    }
}




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedTopAppBar(title: String, showTitle: Boolean, onRefresh: () -> Unit, gradientColors: List<Color>) {
    TopAppBar(
        title = {
            AnimatedVisibility(
                visible = showTitle,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.graphicsLayer(
                        scaleX = 1.2f,
                        scaleY = 1.2f
                    )
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent
        ),
        actions = {
            IconButton(onClick = onRefresh) {
                val rotationState = remember { Animatable(0f) }
                LaunchedEffect(Unit) {
                    rotationState.animateTo(
                        targetValue = 360f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(2000, easing = LinearEasing)
                        )
                    )
                }
                Icon(
                    Icons.Filled.Refresh,
                    contentDescription = "Refresh",
                    tint = Color.White,
                    modifier = Modifier.rotate(rotationState.value)
                )
            }
        },
        modifier = Modifier.background(Brush.horizontalGradient(gradientColors))
    )
}

@Composable
fun EnhancedBottomNavigationBar(viewModel: MangaViewModel, gradientColors: List<Color>) {
    AnimatedVisibility(
        visible = true,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
    ) {
        BottomAppBar(
            modifier = Modifier.background(Brush.horizontalGradient(gradientColors)),
            containerColor = Color.Transparent
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FunkyNavigationButton(
                    icon = Icons.Filled.KeyboardArrowLeft,
                    text = "Prev",
                    onClick = { viewModel.previousPage() },
                    enabled = viewModel.currentPage > 1
                )
                Text(
                    "Page ${viewModel.currentPage}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.graphicsLayer(
                        scaleX = 1.2f,
                        scaleY = 1.2f
                    )
                )
                FunkyNavigationButton(
                    icon = Icons.Filled.KeyboardArrowRight,
                    text = "Next",
                    onClick = { viewModel.nextPage() }
                )
            }
        }
    }
}

@Composable
fun FunkyNavigationButton(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    val bounceState = remember { Animatable(1f) }
    LaunchedEffect(Unit) {
        bounceState.animateTo(
            targetValue = 1.1f,
            animationSpec = infiniteRepeatable(
                animation = tween(500, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
    }

    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White.copy(alpha = 0.2f),
            contentColor = Color.White
        ),
        modifier = Modifier.scale(bounceState.value)
    ) {
        Icon(icon, contentDescription = text)
        Spacer(Modifier.width(4.dp))
        Text(text)
    }
}

@Composable
fun BouncingFAB(onClick: () -> Unit) {
    val bounceState = remember { Animatable(1f) }
    LaunchedEffect(Unit) {
        bounceState.animateTo(
            targetValue = 1.2f,
            animationSpec = infiniteRepeatable(
                animation = tween(500, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
    }

    FloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.secondary,
        modifier = Modifier.scale(bounceState.value)
    ) {
        Icon(Icons.Filled.KeyboardArrowUp, "Scroll to top")
    }
}

@Composable
fun ErrorView(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center){}
}