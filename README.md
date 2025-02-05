Java-Based IT Incident Analysis System for Consulting Firms

Overview:

This project is a Java-based Data Analysis Management System designed to streamline the reporting, tracking, and resolution of IT incidents within consulting firms. The system automates key processes, enhances transparency, and provides valuable insights through data analysis, helping organizations maintain high standards of IT performance.

Key Features:

Data Ingestion: Import data from various sources (CSV, Excel, databases), clean and preprocess data, and store it in a structured format (PostgreSQL). Data Exploration and Analysis: Perform descriptive statistics, data visualization, and advanced analysis (correlation, regression, time-series analysis). Machine Learning Integration: Integrate machine learning algorithms for predictive modeling and classification tasks. Reporting and Dashboard: Generate customizable reports and dashboards to visualize key metrics and analysis outcomes. Security: Implement Basic Authentication with hashed password storage and a JavaFX-based login interface. User-Friendliness: Provide a simple and intuitive interface with guided workflows, tooltips, and clear error messages. System Architecture

The system is built on a layered architecture model, consisting of three primary layers:

Presentation Layer: Handles user interface and interaction, providing data input forms, visualizations, and dashboards. Business Logic Layer: Contains core functionality for data processing, analysis, and reporting, including data cleaning, preprocessing, and machine learning integration. Data Access Layer: Manages database operations, ensuring efficient and secure data storage and retrieval. Technology Stack

Programming Language: Java User Interface Framework: JavaFX Database: PostgreSQL Security Libraries: JDBC Data Analysis Libraries: Apache Spark, Tribuo, MLlib, Apache Commons Math, Weka, Deeplearning4j Visualization Libraries: JFreeChart

System Design:

The system design leverages core object-oriented programming (OOP) principles, including polymorphism, abstraction, inheritance, and encapsulation. Key components include:

Interfaces: DataOperations, VisualizationOperations Abstract Classes: AbstractDataHandler, AbstractDataAnalyzer Concrete Classes: CSVDataHandler, DataAnalyzer, DataVisualizer

Team Members:

Ranim Nasri Khawla Gharbi Arij Sayhi Malek Zouari

Instructor: Ameni Azzouz

Course: Object-Oriented Programming

Acknowledgments

We would like to thank our instructor, Ameni Azzouz, for her guidance and support throughout this project.
