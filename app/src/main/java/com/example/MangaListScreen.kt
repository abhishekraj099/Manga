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
import androidx.compose.ui.text.style.TextAlign
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import kotlinx.coroutines.delay

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
        Color(0xFF051F45),  // Dark Blue
        Color(0xFF2A3E66),  // Medium Blue
        Color(0xFF4A6A8A)   // Lighter Blue
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
        },
        floatingActionButtonPosition = FabPosition.End
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedTopAppBar(title: String, showTitle: Boolean, onRefresh: () -> Unit, gradientColors: List<Color>) {
    TopAppBar(
        title = {
            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .graphicsLayer(
                        scaleX = if (showTitle) 1.2f else 1f,
                        scaleY = if (showTitle) 1.2f else 1f,
                        alpha = if (showTitle) 1f else 0.7f
                    )
                    .animateContentSize()
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent
        ),
        actions = {
            IconButton(onClick = onRefresh) {
                Icon(
                    Icons.Filled.Refresh,
                    contentDescription = "Refresh",
                    tint = Color.White
                )
            }
        },
        modifier = Modifier.background(Brush.horizontalGradient(gradientColors))
    )
}

@Composable
fun LoadingView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center // Align content to the center
    ) {
        LottieLoadingAnimation()
    }
}


@Composable
fun LottieLoadingAnimation() {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.animationlottie))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever,
        isPlaying = true
    )

    LottieAnimation(
        composition = composition,
        progress = { progress },
        modifier = Modifier.size(100.dp)
    )
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
                    .animateItemPlacement()
                    .animateContentSize()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedMangaCard(manga: Manga, onItemClick: () -> Unit, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        onClick = {
            expanded = !expanded
            if (!expanded) onItemClick()
        },
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A59CE)) // Update to the specified color
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                AsyncImage(
                    model = manga.images.jpg.large_image_url,
                    contentDescription = "${manga.title} cover",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                                startY = 100f
                            )
                        )
                )
                Text(
                    text = manga.title,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MangaRating(score = manga.score)
                MangaGenres(genres = manga.genres)
            }
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Synopsis",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.White // Change synopsis title color for better contrast
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = manga.synopsis ?: "No synopsis available",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.8f), // Update synopsis text color
                        maxLines = if (expanded) Int.MAX_VALUE else 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}



@Composable
fun MangaRating(score: Float?) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Filled.Star,
            contentDescription = "Score",
            tint = Color(0xFFFFC107),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = score?.let { String.format("%.2f", it) } ?: "N/A",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFFC107)
        )
    }
}

@Composable
fun MangaGenres(genres: List<Genre>) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        genres.take(3).forEach { genre ->
            Chip(
                onClick = { /* TODO: Filter by genre */ },
                colors = ChipDefaults.chipColors(
                    backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                label = { Text(genre.name, fontSize = 12.sp) }
            )
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun EnhancedBottomNavigationBar(viewModel: MangaViewModel, gradientColors: List<Color>) {
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
            AnimatedContent(
                targetState = viewModel.currentPage,
                transitionSpec = {
                    slideInVertically { height -> height } + fadeIn() with
                            slideOutVertically { height -> -height } + fadeOut()
                }
            ) { page ->
                Text(
                    "Page $page",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.graphicsLayer(
                        scaleX = 1.2f,
                        scaleY = 1.2f
                    )
                )
            }
            FunkyNavigationButton(
                icon = Icons.Filled.KeyboardArrowRight,
                text = "Next",
                onClick = { viewModel.nextPage() }
            )
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
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White.copy(alpha = 0.2f),
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Icon(icon, contentDescription = text)
        Spacer(Modifier.width(4.dp))
        Text(text)
    }
}

@Composable
fun BouncingFAB(onClick: () -> Unit) {
    val scale = remember { Animatable(1f) }
    LaunchedEffect(Unit) {
        while (true) {
            scale.animateTo(1.2f, animationSpec = tween(300))
            scale.animateTo(1f, animationSpec = tween(300))
            delay(1000)
        }
    }
    FloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.secondary,
        modifier = Modifier.scale(scale.value)
    ) {
        Icon(Icons.Filled.KeyboardArrowUp, "Scroll to top")
    }
}

@Composable
fun ErrorView(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = "Error",
            tint = Color.Red,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            color = Color.White,
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Text("Retry")
        }
    }
}