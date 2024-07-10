import os
import numpy as np
import tensorflow as tf
from tensorflow.keras.preprocessing.image import ImageDataGenerator
from tensorflow.keras.callbacks import ModelCheckpoint, EarlyStopping
from sklearn.model_selection import train_test_split
import matplotlib.pyplot as plt
import cv2

# Function to load HOMUS dataset
def load_homus_dataset(dataset_path):
    images = []
    labels = []
    class_labels = {class_dir: idx for idx, class_dir in enumerate(os.listdir(dataset_path))}
    for class_dir in os.listdir(dataset_path):
        class_path = os.path.join(dataset_path, class_dir)
        if os.path.isdir(class_path):
            for file in os.listdir(class_path):
                if file.endswith('.png'):
                    img_path = os.path.join(class_path, file)
                    image = plt.imread(img_path)
                    if image.ndim == 3:  # Convert RGB to grayscale if needed
                        image = image[:, :, 0]
                    image = cv2.resize(image, (128, 128)) / 255.0  # Resize image to 64x64 and normalize
                    images.append(image)
                    labels.append(class_labels[class_dir])
    images = np.array(images)
    labels = np.array(labels)
    return images, labels, len(class_labels)

# Load dataset
dataset_path = 'C:\\Users\\xavie\\Documents\\GitFolder\\OMRModelTraining\\Homus-Dataset'  # Replace with the path to your HOMUS dataset
images, labels, num_classes = load_homus_dataset(dataset_path)

# Debug statements to check if images and labels are loaded correctly
print(f"Number of images loaded: {len(images)}")
print(f"Number of labels loaded: {len(labels)}")
print(f"Number of classes: {num_classes}")

if len(images) == 0 or len(labels) == 0:
    raise ValueError("No images or labels were loaded. Please check the dataset path and structure.")

# Preprocess images
images = np.expand_dims(images, axis=-1)  # Add channel dimension
labels = tf.keras.utils.to_categorical(labels, num_classes=num_classes)  # Convert labels to categorical

# Split the data into training, validation, and testing sets
X_train, X_temp, y_train, y_temp = train_test_split(images, labels, test_size=0.3, random_state=42)
X_val, X_test, y_val, y_test = train_test_split(X_temp, y_temp, test_size=0.5, random_state=42)

# Define data augmentation
datagen = ImageDataGenerator(
    rotation_range=20,
    zoom_range=0.3,
    width_shift_range=0.3,
    height_shift_range=0.3,
    #horizontal_flip=True
)

# Define callbacks with checkpoint saving and early stopping
model_checkpoint = ModelCheckpoint('best_omr_model.keras', save_best_only=True, monitor='val_loss', save_freq='epoch')
early_stopping = EarlyStopping(monitor='val_loss', patience=15, restore_best_weights=True)

# Load or initialize model

#------------------------------------#
# esseyer model preentrainer efficientnet
#------------------------------------#

model_path = 'omr_model.keras'
if os.path.exists(model_path):
    model = tf.keras.models.load_model(model_path)
    print("Loaded existing model.")
else:
    # Define the CNN model with increased complexity and batch normalization
    model = tf.keras.models.Sequential([
        tf.keras.layers.Input(shape=(128, 128, 1)),  # Correctly define input shape
        tf.keras.layers.Conv2D(32, (3, 3), activation='relu'),
        tf.keras.layers.BatchNormalization(),
        tf.keras.layers.MaxPooling2D((2, 2)),
        tf.keras.layers.Conv2D(64, (3, 3), activation='relu'),
        tf.keras.layers.BatchNormalization(),
        tf.keras.layers.MaxPooling2D((2, 2)),
        tf.keras.layers.Conv2D(128, (3, 3), activation='relu'),
        tf.keras.layers.BatchNormalization(),
        tf.keras.layers.MaxPooling2D((2, 2)),
        tf.keras.layers.Conv2D(256, (3, 3), activation='relu'),
        tf.keras.layers.BatchNormalization(),
        tf.keras.layers.MaxPooling2D((2, 2)),
        tf.keras.layers.Flatten(),
        tf.keras.layers.Dense(512, activation='relu'),
        tf.keras.layers.Dropout(0.5),
        tf.keras.layers.Dense(num_classes, activation='softmax')
    ])
    print("Initialized new model.")
    
model.summary()

# Compile the model with a higher learning rate
model.compile(optimizer=tf.keras.optimizers.Adam(learning_rate=0.00001),
              loss='categorical_crossentropy',
              metrics=['accuracy'])

# Function to continuously train the model
def continuous_training():
    batch_size = 32
    epochs = 100
    try:
        while True:
            history = model.fit(datagen.flow(X_train, y_train, batch_size=batch_size),
                                validation_data=(X_val, y_val),
                                epochs=epochs,
                                callbacks=[model_checkpoint, early_stopping],
                                verbose=1)
            
            # Save the model at the end of each training cycle
            model.save(model_path)
            print("Model saved. Starting next training cycle.")
            
            # Print out training and validation metrics
            print(f"Training accuracy: {history.history['accuracy'][-1]}")
            print(f"Validation accuracy: {history.history['val_accuracy'][-1]}")
            print(f"Training loss: {history.history['loss'][-1]}")
            print(f"Validation loss: {history.history['val_loss'][-1]}")
    except KeyboardInterrupt:
        print("Training interrupted. Model saved.")
        model.save(model_path)

if __name__ == "__main__":
    continuous_training()
