% Generated by roxygen2 (4.1.1): do not edit by hand
% Please edit documentation in R/generate_qgs.r
\name{generate_qgs}
\alias{generate_qgs}
\title{Create a QGIS project (.qgs file) with all the .asc and all the .shp files found in the folder}
\usage{
generate_qgs(folder, file, EPSG, Google = TRUE)
}
\arguments{
\item{folder}{string, folder to scan}

\item{file}{string, name of the QGIS project file}

\item{EPSG}{list, EPSG description of the reference systeme used as destination}

\item{Google}{boolean, true to add a Google Streets Layer at the QGIS Project}
}
\value{
nothing
}
\description{
Create a QGIS project (.qgs file) with all the .asc and all the .shp files found in the folder
}
\examples{
generate_qgs('data/generated/', 'qgis_project.qgs', EPSG, FALSE)
}
\author{
Valentin SASYAN, v. 1.2.0, 06/22/2015
}

