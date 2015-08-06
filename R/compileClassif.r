#' Copile the types given by the classification
#' @param proj.classif SpatialPointsDataFrame, the points classification
#' @return proj.compile SpatialPointsDataFrame, the compiled classification
#' @author
#' Valentin SASYAN, v. 1.0.0, 07/28/2015
compileClassif <- function(proj.classif) {
	classif <- as.data.frame(subset(proj.classif, svm == 'classif'))
	listId <- unique(classif$id)
	proj.compile <- NULL

	for (Id in listId) {
		classifTemp <- subset(classif, id == Id)
		factor <- factor(classifTemp$type)
		s <- sort(summary(factor), decreasing=T)
		chosenType <- as.integer(labels(s)[[1]])
		classifTemp$type <- chosenType
		if (is.null(proj.compile)) {
			proj.compile <- classifTemp
		} else {proj.compile <- rbind(proj.compile, classifTemp)}
	}

	proj.compile <- SpatialPointsDataFrame(coords=proj.compile[9:10], data=proj.compile[,-c(9,10)])
}