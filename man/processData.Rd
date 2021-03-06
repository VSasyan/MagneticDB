% Generated by roxygen2 (4.1.1): do not edit by hand
% Please edit documentation in R/processData.r
\name{processData}
\alias{processData}
\title{Process (interpolation + classification) the data generated by the Android App and exported in the JSON file}
\usage{
processData(filter = "uOttawa", resolution = 0, export = TRUE,
  erase = TRUE, EPSG = EPSG_, p = 0.5, classif = TRUE,
  compile = FALSE, useX = FALSE, lessType = FALSE, rePredict = FALSE,
  debug = FALSE)
}
\arguments{
\item{filter}{RegExpr, filter for the name of the folders to use}

\item{resolution}{double, resolution of the grid of interpolation (size of 1 px in real), 0 for no interpolation}

\item{export}{bool, true if the function have to export the used data}

\item{erase}{bool, true if the function can erase existing used data}

\item{EPSG}{list, EPSG description of the reference systeme used as destination}

\item{p}{real, power p for the interpolation}

\item{classif}{boolean, do or not the the classification}

\item{compile}{boolean, do or not the compilation of the classification}

\item{useX}{boolean, use axe X for the classification}

\item{lessType}{integer, 0 : no merge, 1 : merge similar types, 2 : merge "wood / not wood"}

\item{rePredict}{boolean, the known types can be modified during the classification}

\item{debug}{boolean, debug mode (more printed information)}
}
\value{
nothing
}
\description{
Process (interpolation + classification) the data generated by the Android App and exported in the JSON file
}
\examples{
generate_qgs(filter='uOttawa.*',resolution=1,erase=FALSE)
}
\author{
Valentin SASYAN, v. 0.9.0, 07/28/2015
}

