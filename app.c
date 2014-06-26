#include <pebble.h>

static Window *window;
static TextLayer *text_layer;


//Message receive handlers
void out_sent_handler(DictionaryIterator *sent, void *context) {
   // outgoing message was delivered
}


void out_failed_handler(DictionaryIterator *failed, AppMessageResult reason, void *context) {
   // outgoing message failed
}


void in_received_handler(DictionaryIterator *received, void *context) {
   // incoming message received
}


void in_dropped_handler(AppMessageResult reason, void *context) {
   // incoming message dropped
}
//End receive handlers

void accel_tap_handler(AccelAxisType axis, int32_t direction) {
  if(axis == 0){
   text_layer_set_text(text_layer, "X");
 } else if (axis == 1){
   text_layer_set_text(text_layer, "Y");
 } else if(axis == 2) {
   text_layer_set_text(text_layer, "Z");
 } else{
   text_layer_set_text(text_layer, "not working");
 }

 //Send axis data
 DictionaryIterator *iter;
 app_message_outbox_begin(&iter);

 Tuplet value = TupletInteger(1, axis);
 dict_write_tuplet(iter, &value);

 app_message_outbox_send();

 accel_tap_service_unsubscribe();
 text_layer_set_text(text_layer, "Press up to listen");

}

static void down_click_handler(ClickRecognizerRef recognizer, void *context) {
 accel_tap_service_unsubscribe();
 text_layer_set_text(text_layer, "Press up to listen");
}

static void up_click_handler(ClickRecognizerRef recognizer, void *context) {
  accel_tap_service_subscribe(&accel_tap_handler);
  text_layer_set_text(text_layer, "Listening");
}


static void click_config_provider(void *context) {
  window_single_click_subscribe(BUTTON_ID_UP, up_click_handler);
  window_single_click_subscribe(BUTTON_ID_DOWN, down_click_handler);

}

static void window_load(Window *window) {
  Layer *window_layer = window_get_root_layer(window);
  GRect bounds = layer_get_bounds(window_layer);

  text_layer = text_layer_create((GRect) { .origin = { 0, 72 }, .size = { bounds.size.w, 20 } });
  text_layer_set_text(text_layer, "Press up to listen");
  text_layer_set_text_alignment(text_layer, GTextAlignmentCenter);
  layer_add_child(window_layer, text_layer_get_layer(text_layer));
}

static void window_unload(Window *window) {
  text_layer_destroy(text_layer);
}

static void init(void) {
  window = window_create();
  window_set_click_config_provider(window, click_config_provider);
  window_set_window_handlers(window, (WindowHandlers) {
    .load = window_load,
    .unload = window_unload,
  });
  app_message_register_inbox_received(in_received_handler);
  app_message_register_inbox_dropped(in_dropped_handler);
  app_message_register_outbox_sent(out_sent_handler);
  app_message_register_outbox_failed(out_failed_handler);
  const uint32_t inbound_size = 64;
  const uint32_t outbound_size = 64;
  app_message_open(inbound_size, outbound_size);
  const bool animated = true;
  window_stack_push(window, animated);
}

static void deinit(void) {
  window_destroy(window);
  accel_tap_service_unsubscribe();
}

int main(void) {
  init();

  APP_LOG(APP_LOG_LEVEL_DEBUG, "Done initializing: %p", window);

  app_event_loop();
  deinit();
}